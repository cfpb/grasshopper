package grasshopper.addresspoints

import scala.util.Properties
import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import io.geojson.FeatureJsonProtocol._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{ HttpResponse, HttpRequest }
import akka.http.model.MediaTypes._
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.unmarshalling.Unmarshal
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.math._
import spray.json.DefaultJsonProtocol
import java.util.Calendar
import grasshopper.elasticsearch.Geocode
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import spray.json._

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

  val routes = {
    path("status") {
      get {
        compressResponseIfRequested() {
          complete {
            val now = Calendar.getInstance().getTime()
            ToResponseMarshallable(Status("OK", now.toString))
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
