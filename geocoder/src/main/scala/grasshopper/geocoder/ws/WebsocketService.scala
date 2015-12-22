package grasshopper.geocoder.ws

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.stream.{ FlowShape, ActorMaterializer }
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import grasshopper.geocoder.api.stats.GeocodeStatsPublisher
import grasshopper.geocoder.model.GeocodeStats
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import scala.concurrent.ExecutionContextExecutor
import spray.json._

trait WebsocketService extends GrasshopperJsonProtocol {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def geocodeStats: Flow[Message, Message, Unit] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit b: GraphDSL.Builder[Unit] =>
        import GraphDSL.Implicits._

        val merge = b.add(Merge[String](2))
        val msgToString = b.add(Flow[Message].map(msg => ""))
        val statToString = b.add(Flow[GeocodeStats].map(_.toJson.toString))
        val stringToMsg = b.add(Flow[String].map(s => TextMessage(s)))

        val source = Source.actorPublisher(GeocodeStatsPublisher.props)

        msgToString ~> merge
        source ~> statToString ~> merge ~> stringToMsg

        FlowShape(msgToString.in, stringToMsg.outlet)

      }
    )

  }

  val wsRoutes =
    path("metrics-ws") {
      handleWebsocketMessages(geocodeStats)
    }

  object WSActor {
    def props: Props = Props(new WSActor)
  }

  class WSActor extends ActorPublisher[TextMessage] {
    override def receive: Receive = {
      case tm: TextMessage =>
        sender() ! TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    }
  }
}
