import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{ HttpResponse, HttpRequest }
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.unmarshalling.Unmarshal
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.math._
import spray.json.DefaultJsonProtocol
import java.util.Calendar

case class Status(status: String, time: String)

case class AddressInput(id: Int, address: String)

trait Protocols extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat2(Status.apply)
  implicit val addressInputFormat = jsonFormat2(AddressInput.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: FlowMaterializer

  def config: Config
  val logger: LoggingAdapter

  val routes = {
    logRequestResult("akka-http-microservice") {
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
                entity(as[AddressInput]) { address =>
                  //val point = geocodePoint(client, "address", "point", address)
                  complete {
                    //ToResponseMarshallable(point)
                    ToResponseMarshallable(address)
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
  override implicit val materializer = FlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith(routes)
}
