package addresspoints.api

import java.net.InetAddress
import java.time.Instant
import addresspoints.model.{ AddressInput, Status }
import addresspoints.protocol.JsonProtocol
import addresspoints.search.Geocode
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import spray.json._
import scala.concurrent.ExecutionContextExecutor

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
        encodeResponseWith(NoCoding, Gzip, Deflate) {
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
            encodeResponseWith(NoCoding, Gzip, Deflate) {
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
                    encodeResponseWith(NoCoding, Gzip, Deflate) {
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
    val points = geocode(client, "address", "point", address, count) getOrElse (Nil.toArray)
    if (points.length > 0) {
      complete {
        ToResponseMarshallable(points)
      }
    } else {
      complete(NotFound)
    }

  }
}
