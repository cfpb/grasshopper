package addresspoints.api

import java.net.InetAddress
import java.time.Instant

import addresspoints.model.{ AddressInput, Status }
import addresspoints.protocol.JsonProtocol
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.marshalling.ToResponseMarshallable
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.server.StandardRoute
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import grasshopper.elasticsearch.Geocode
import org.elasticsearch.client.Client
import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }
import spray.json._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import io.geojson.FeatureJsonProtocol._

trait Service extends JsonProtocol with Geocode {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: ActorFlowMaterializer
  implicit val client: Client

  def config: Config

  val logger: LoggingAdapter

  override lazy val log = Logger(LoggerFactory.getLogger("grasshopper-address-points"))

  val routes = {
    path("status") {
      get {
        compressResponseIfRequested() {
          complete {
            // Creates ISO-8601 date string in UTC down to millisecond precision
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", now, host)
            log.debug(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      pathPrefix("addresses") {
        pathPrefix("points") {
          post {
            compressResponseIfRequested() {
              entity(as[String]) { json =>
                try {
                  val addressInput = json.parseJson.convertTo[AddressInput]
                  geocodePoints(addressInput.address, 1)
                } catch {
                  case e: spray.json.DeserializationException =>
                    complete(BadRequest)
                }
              }
            }
          } ~
            get {
              path(Segment) { address =>
                parameters('suggest.as[Int] ? 1) { suggest =>
                  get {
                    compressResponseIfRequested() {
                      geocodePoints(address, suggest)
                    }
                  }
                }
              }
            }
        }
      }
  }

  private def geocodePoints(address: String, count: Int): StandardRoute = {
    val points = geocode(client, "address", "point", address, count)
    points match {
      case Success(pts) =>
        if (pts.size > 0) {
          complete {
            ToResponseMarshallable(pts)
          }
        } else {
          complete(NotFound)
        }
      case Failure(_) =>
        complete {
          NotFound
        }
    }
  }
}
