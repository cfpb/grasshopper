package grasshopper.addresspoints

import java.util.Calendar

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.{ Config, ConfigFactory }
import grasshopper.elasticsearch.Geocode
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import spray.json._
import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

case class Status(status: String, time: String)

case class AddressInput(id: Int, address: String)

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat2(Status.apply)
  implicit val addressInputFormat = jsonFormat2(AddressInput.apply)
}

trait Service extends JsonProtocol with Geocode {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorFlowMaterializer
  implicit val client: Client

  def config: Config
  val logger: LoggingAdapter

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-addresspoints"))

  val routes = {
    path("status") {
      get {
        compressResponseIfRequested() {
          complete {
            val now = Calendar.getInstance().getTime()
            val status = Status("OK", now.toString)
            log.info(status.toJson.toString())
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      pathPrefix("address") {
        path("point") {
          post {
            compressResponseIfRequested() {
              entity(as[String]) { json =>
                val addressInput = json.parseJson.convertTo[AddressInput]
                val point = geocodePoint(client, "address", "point", addressInput.address)
                point match {
                  case Some(p) =>
                    complete {
                      ToResponseMarshallable(point)
                    }
                  case None =>
                    complete {
                      NotFound
                    }
                }
              }
            }
          }
        }
      }
  }
}

object AddressPointService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = Properties.envOrElse("ELASTICSEARCH_HOST", config.getString("elasticsearch.host"))
  lazy val port = Properties.envOrElse("ELASTICSEARCH_PORT", config.getString("elasticsearch.port"))
  lazy val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith(routes)
}
