package grasshopper.api

import java.net.InetAddress
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import org.slf4j.LoggerFactory
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import akka.event.LoggingAdapter
import grasshopper.protocol.GrasshopperJsonProtocol
import grasshopper.model.Status
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import spray.json._
import io.geojson.FeatureJsonProtocol._

trait Service extends GrasshopperJsonProtocol {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorFlowMaterializer

  def config: Config

  val logger: LoggingAdapter

  lazy val log = Logger(LoggerFactory.getLogger("grashopper-geocoder"))

  val routes = {
    path("status") {
      get {
        encodeResponseWith(NoCoding, Gzip, Deflate) {
          complete {
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", "grasshopper-geocoder", now, host)
            log.debug(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    }
  }

}
