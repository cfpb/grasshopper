package grasshopper.geocoder.http

import java.net.URLDecoder
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpCharsets, HttpEntity }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.ContentNegotiator.Alternative.MediaType
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.Config
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParserStatus
import grasshopper.geocoder.api.geocode.GeocodeFlow
import grasshopper.geocoder.model.GeocodeStatus
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import scala.async.Async.{ async, await }
import scala.concurrent.ExecutionContextExecutor
import spray.json._
import io.geojson.FeatureJsonProtocol._

trait HttpService extends GrasshopperJsonProtocol with GeocodeFlow {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def config: Config
  def client: Client

  val logger: LoggingAdapter

  val routes = {
    pathSingleSlash {
      val fStatus = async {
        val ps = AddressParserClient.status.map(s => s.right.getOrElse(ParserStatus.empty))
        GeocodeStatus(await(ps))
      }

      encodeResponseWith(NoCoding, Gzip, Deflate) {
        complete {
          fStatus.map { s =>
            ToResponseMarshallable(s)
          }
        }
      }
    } ~
      path("geocode") {
        post {
          entity(as[FormData]) { formData =>
            complete {
              val source = formData.parts
                .mapAsync(numCores) { bodyPart =>
                  bodyPart
                    .entity
                    .dataBytes
                    .runFold(ByteString.empty)(_ ++ _)
                    .map(contents => contents)
                }

              val lineStream = source.via(
                Framing.delimiter(
                  ByteString("\n"),
                  maximumFrameLength = 100,
                  allowTruncation = true
                )
              ).map(_.utf8String)

              val gFlow = lineStream
                .via(geocodeFlow)
                .via(filterPointResultFlow)
                .via(featureToCsv)
                .map(c => c + "\n")

              val geocodeByteStream = gFlow.map(s => ByteString(s))
              HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), geocodeByteStream)
            }
          }
        }
      } ~
      path("geocode" / Segment) { address =>
        val a = URLDecoder.decode(address, "UTF-8")
        complete {
          val source = Source.single(a)
          val gFlow = source
            .via(geocodeFlow)

          val geocodeFlowByteStream = gFlow.map(s => ByteString(s.toJson.toString))
          HttpEntity.Chunked.fromData(`application/json`, geocodeFlowByteStream)
        }
      }
  }

}
