package grasshopper.geocoder.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import grasshopper.client.parser.AddressParserClient
import grasshopper.client.parser.model.ParserStatus
import grasshopper.geocoder.api.GeocodeFlow
import grasshopper.geocoder.model.GeocodeStatus1
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol1
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import scala.async.Async.{ async, await }
import scala.concurrent.ExecutionContextExecutor
import spray.json._
import io.geojson.FeatureJsonProtocol._

trait HttpService1 extends GrasshopperJsonProtocol1 with GeocodeFlow {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def config: Config
  def client: Client

  val logger: LoggingAdapter

  lazy val log = Logger(LoggerFactory.getLogger("grashopper-geocoder"))

  val routes = {
    pathSingleSlash {
      val fStatus = async {
        val ps = AddressParserClient.status.map(s => s.right.getOrElse(ParserStatus.empty))
        GeocodeStatus1(await(ps))
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
                .mapAsync(4) { bodyPart =>
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

              val geocodeByteStream = gFlow.map(s => ByteString(s))
              HttpEntity.Chunked.fromData(`text/csv`, geocodeByteStream)
            }
          }
        }
      } ~
      path("geocode" / Segment) { address =>
        complete {
          val source = Source.single(address)
          val gFlow = source
            .via(geocodeFlow)

          val geocodeFlowByteStream = gFlow.map(s => ByteString(s.toJson.toString))
          HttpEntity.Chunked.fromData(`application/json`, geocodeFlowByteStream)
        }
      }
  }

}
