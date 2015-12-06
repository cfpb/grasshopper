package grasshopper.geocoder.ws

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.model.ws.{ BinaryMessage, TextMessage, Message }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{ Sink, Source, Flow }

import grasshopper.geocoder.protocol.GrasshopperJsonProtocol

import scala.concurrent.ExecutionContextExecutor

trait WebsocketService extends GrasshopperJsonProtocol {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage ⇒
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
        //Source.actorPublisher()
      case bm: BinaryMessage ⇒
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  val wsRoutes =
    path("greeter") {
      handleWebsocketMessages(greeter)
    }


  object WSActor {
    def props: Props = Props(new WSActor)
  }

  class WSActor extends ActorPublisher[TextMessage] {
    override def receive: Receive = {
      case tm:TextMessage =>
        sender() ! TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    }
  }
}
