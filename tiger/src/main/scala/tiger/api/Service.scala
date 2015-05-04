package tiger.api

import java.net.InetAddress
import java.time.Instant
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import elasticsearch.TigerGeocode
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import tiger.model.Status
import tiger.protocol.JsonProtocol
import spray.json._

import scala.concurrent.ExecutionContextExecutor

trait Service extends JsonProtocol with TigerGeocode {
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
            // Creates ISO-8601 date string in UTC down to millisecond precision
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", now, host)
            log.debug(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    }

  }
}
