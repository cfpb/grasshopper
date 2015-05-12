package grasshopper.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.concurrent.duration._

trait ServiceClient {

  implicit val askTimeout: Timeout = 1000.millis

  implicit val system: ActorSystem
  implicit val materializer: ActorFlowMaterializer

  val config: Config

  def sendGetRequest(host: String, port: Int, path: String): Future[HttpResponse] = {
    val connectionFlow = Http().outgoingConnection(host, port)
    val request = HttpRequest(GET, path)
    Source.single(request).via(connectionFlow).runWith(Sink.head)
  }

}
