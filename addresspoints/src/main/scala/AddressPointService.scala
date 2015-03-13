package grasshopper.addresspoints

import java.util.Calendar

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.StatusCodes.{ NotFound, InternalServerError }
import akka.http.server.Directives._
import akka.http.server.StandardRoute
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.{ Config, ConfigFactory }
import grasshopper.elasticsearch.Geocode
import io.geojson.FeatureJsonProtocol._
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import spray.json._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Success, Failure, Properties }
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

  override lazy val log = Logger(LoggerFactory.getLogger("grasshopper-address-points"))

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
      pathPrefix("addresses") {

        path("points") {
          get {
            compressResponseIfRequested() {
              parameter('search.as[String]) { address =>
                geocodePoint(address)
              }
            }
          } ~
            post {
              compressResponseIfRequested() {
                entity(as[String]) { json =>
                  val addressInput = json.parseJson.convertTo[AddressInput]
                  geocodePoint(addressInput.address)
                }
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
