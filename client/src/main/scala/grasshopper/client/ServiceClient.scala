package grasshopper.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, StreamTcpException }
import akka.util.Timeout
import com.typesafe.config.Config
import grasshopper.client.model.ResponseError
import grasshopper.client.protocol.ClientJsonProtocol
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait ServiceClient extends ClientJsonProtocol {
  implicit val askTimeout: Timeout = 1000.millis
  implicit val system: ActorSystem = ActorSystem("grasshopper-client")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def host: String
  def port: String

  val config: Config

  def sendGetRequest(path: Uri): Future[HttpResponse] = {
    implicit val ec: ExecutionContext = system.dispatcher
    val connectionFlow = Http().outgoingConnection(host, port.toInt)
    val request = HttpRequest(GET, path)
    Source.single(request).via(connectionFlow).runWith(Sink.head).recover {
      case e: StreamTcpException =>
        HttpResponse(
          ServiceUnavailable,
          Nil,
          HttpEntity.empty(ContentTypes.NoContentType),
          HttpProtocols.`HTTP/1.1`
        )
    }
  }

  def sendResponseError(response: HttpResponse) = {
    Future.successful {
      Left(ResponseError(response.status.toString()))
    }
  }

}
