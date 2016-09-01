package grasshopper.geocoder.api.stats

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.ActorPublisher
import grasshopper.geocoder.model.GeocodeStats
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import spray.json._

import scala.collection.mutable

object GeocodeStatsPublisher {
  case class PublishStats()
  def props: Props = Props(new GeocodeStatsPublisher)
}

class GeocodeStatsPublisher extends ActorPublisher[GeocodeStats] with ActorLogging with GrasshopperJsonProtocol {

  var stats = mutable.Queue[GeocodeStats]()

  override def preStart(): Unit = {
    log.info("Starting GeocodeStatsPublisher")
    context.system.eventStream.subscribe(self, classOf[GeocodeStats])
  }

  override def receive: Receive = {
    case g: GeocodeStats =>
      log.debug(g.toJson.toString)
      onNext(g)
    case msg: Any => // ignore other messages

  }
}
