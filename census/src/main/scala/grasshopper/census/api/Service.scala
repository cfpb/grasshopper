package grasshopper.census.api

import java.net.InetAddress
import java.time.Instant
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import feature.Feature
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import grasshopper.census.model.{ ParsedInputAddress, Status }
import grasshopper.census.protocol.CensusJsonProtocol
import spray.json._
import io.geojson.FeatureJsonProtocol._
import grasshopper.census.search.CensusGeocode
import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Success, Failure, Try }

trait Service extends CensusJsonProtocol with CensusGeocode {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: ActorFlowMaterializer
  implicit val client: Client

  def config: Config

  val logger: LoggingAdapter

  override lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census"))

  val routes = {
    path("status") {
      get {
        encodeResponseWith(NoCoding, Gzip, Deflate) {
          complete {
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", "grasshopper-census", now, host)
            log.debug(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      pathPrefix("census") {
        path("addrfeat") {
          get {
            parameters(
              'number.as[Int] ? 0,
              'streetName.as[String] ? "",
              'zipCode.as[Int] ? 0,
              'state.as[String] ? ""
            ) { (number, streetName, zipCode, state) =>
                val addressInput = ParsedInputAddress(number, streetName, zipCode, state)
                encodeResponseWith(NoCoding, Gzip, Deflate) {
                  geocodeLines(addressInput, 1)
                }
              }
          } ~
            post {
              encodeResponseWith(NoCoding, Gzip, Deflate) {
                entity(as[String]) { json =>
                  val tryAddressInput = Try(json.parseJson.convertTo[ParsedInputAddress])
                  tryAddressInput match {
                    case Success(a) => geocodeLines(a, 1)
                    case Failure(e) =>
                      log.error(e.getLocalizedMessage)
                      complete(BadRequest)
                  }
                }
              }
            }
        }
      }

  }

  private def geocodeLines(addressInput: ParsedInputAddress, count: Int): StandardRoute = {
    val points = geocodeLine(client, "census", "addrfeat", addressInput, count) getOrElse (Nil.toArray)
    if (points.length > 0) {
      complete(ToResponseMarshallable(points))
    } else {
      val pts: Array[Feature] = Nil.toArray
      complete(ToResponseMarshallable(pts))
    }

  }
}
