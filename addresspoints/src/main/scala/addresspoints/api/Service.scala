package addresspoints.api

import java.text.SimpleDateFormat
import java.time.{ ZoneOffset, LocalDateTime, ZoneId }
import java.util.{ TimeZone, Date, Calendar }
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
import scala.util.{ Try, Failure, Success }
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
            val now = formatIso8601(new Date())
            val status = Status("OK", now)
            log.info(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      pathPrefix("addresses") {
        path("points") {
          post {
            compressResponseIfRequested() {
              entity(as[String]) { json =>
                try {
                  val addressInput = json.parseJson.convertTo[AddressInput]
                  geocodePoint(addressInput.address)
                } catch {
                  case e: spray.json.DeserializationException =>
                    complete(BadRequest)
                }
              }
            }
          }
        } ~
          path("points" / Segment) { address =>
            get {
              compressResponseIfRequested() {
                geocodePoint(address)
              }
            }
          }
      }
  }

  private def geocodePoint(address: String): StandardRoute = {
    val point = geocode(client, "address", "point", address)
    point match {
      case Success(p) =>
        complete {
          ToResponseMarshallable(p)
        }
      case Failure(_) =>
        complete {
          NotFound
        }
    }
  }

  /**
   * Translates a [[java.util.Date]] into ISO-8601 formatted with UTC timezone
   */
  private def formatIso8601(date: Date): String = {
    val utc = TimeZone.getTimeZone("UTC")
    val iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    iso8601.setTimeZone(utc)
    iso8601.format(date)
  }

}
