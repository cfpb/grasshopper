package grasshopper.geocoder.api.stats

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorSubscriber, RequestStrategy, WatermarkRequestStrategy }
import grasshopper.geocoder.model.GeocodeResponse
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol

object GeocodeStatsSubscriber {
  def props: Props = Props(new GeocodeStatsSubscriber)
}

class GeocodeStatsSubscriber extends ActorSubscriber with ActorLogging with GrasshopperJsonProtocol {

  override protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(50)

  override def receive: Receive = {
    case OnNext(g: GeocodeResponse) =>
      context.actorSelection("/user/statsAggregator") ! g
    case OnError(err: Exception) =>
      log.error(s"ERROR: ${err.getLocalizedMessage}")
    case OnComplete =>
      log.debug("Geocoding Stream completed")
  }

}
