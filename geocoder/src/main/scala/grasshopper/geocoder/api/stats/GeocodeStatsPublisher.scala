package grasshopper.geocoder.api.stats

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.ActorPublisher
import grasshopper.geocoder.model.GeocodeStats

import scala.collection.mutable

object GeocodeStatsPublisher {
  case class PublishStats()
  def props: Props = Props(new GeocodeStatsPublisher)
}

class GeocodeStatsPublisher extends ActorPublisher[GeocodeStats] with ActorLogging {

  var stats = mutable.Queue[GeocodeStats]()

  override def preStart(): Unit = {
    log.info("Starting GeocodeStatsPublisher")
    context.system.eventStream.subscribe(self, classOf[GeocodeStats])
  }

  override def receive: Receive = {
    case g: GeocodeStats =>
      onNext(g)
    case _ => // ignore other messages
  }
}
