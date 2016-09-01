package grasshopper.geocoder.ws

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl._
import akka.stream.{ ActorMaterializer, FlowShape }
import grasshopper.geocoder.api.stats.GeocodeStatsPublisher
import grasshopper.geocoder.model.GeocodeStats
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import spray.json._

import scala.concurrent.ExecutionContextExecutor

trait WebsocketService extends GrasshopperJsonProtocol {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  def geocodeStats: Flow[Message, Message, NotUsed] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
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
      handleWebSocketMessages(geocodeStats)
    }

}
