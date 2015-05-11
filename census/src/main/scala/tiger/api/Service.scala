package tiger.api

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
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import tiger.model.{ ParsedAddressInput, Status }
import tiger.protocol.CensusJsonProtocol
import spray.json._
import io.geojson.FeatureJsonProtocol._
import tiger.search.CensusGeocode
import scala.concurrent.ExecutionContextExecutor

trait Service extends CensusJsonProtocol with CensusGeocode {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: ActorFlowMaterializer
  implicit val client: Client

  def config: Config

  val logger: LoggingAdapter

  override lazy val log = Logger(LoggerFactory.getLogger("grasshopper-tiger"))

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
                val addressInput = ParsedAddressInput(number, streetName, zipCode, state)
                encodeResponseWith(NoCoding, Gzip, Deflate) {
                  geocodeLines(addressInput, 1)
                }
              }
          } ~
            post {
              encodeResponseWith(NoCoding, Gzip, Deflate) {
                entity(as[String]) { json =>
                  val addressInput = json.parseJson.convertTo[ParsedAddressInput]
                  geocodeLines(addressInput, 1)
                }
              }
            }
        }
      }

  }

  private def geocodeLines(addressInput: ParsedAddressInput, count: Int): StandardRoute = {
    val points = geocodeLine(client, "census", "addrfeat", addressInput, count) getOrElse (Nil.toArray)
    if (points.length > 0)
      complete(ToResponseMarshallable(points))
    else
      complete(NotFound)
  }
}
