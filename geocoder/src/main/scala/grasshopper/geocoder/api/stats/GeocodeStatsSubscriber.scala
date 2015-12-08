package grasshopper.geocoder.api.stats

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorSubscriber, RequestStrategy, WatermarkRequestStrategy }
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStats }
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol

object GeocodeStatsSubscriber {
  def props: Props = Props(new GeocodeStatsSubscriber)
}

class GeocodeStatsSubscriber extends ActorSubscriber with ActorLogging with GrasshopperJsonProtocol {

  var total: Int = 0
  var parsed: Int = 0
  var points: Int = 0
  var census: Int = 0

  override protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(50)

  override def receive: Receive = {
    case OnNext(g: GeocodeResponse) =>
      val stats = computeGeocodeStats(g)
      context.actorSelection("/user/statsAggregator") ! stats
    case OnNext(s: String) =>
      log.info(s"received an input string: ${s}")
    case OnError(err: Exception) =>
      log.error(s"ERROR: ${err.getLocalizedMessage}")
    case OnComplete =>
      log.info("Geocoding Stream completed")
  }

  private def computeGeocodeStats(g: GeocodeResponse): GeocodeStats = {
    total += 1
    val inputAddress = g.input
    val parts = g.parts
    val features = g.features
    val pointFeatures = features.filter(f => f.get("source").getOrElse("") == "state-address-points")
    if (pointFeatures.nonEmpty) {
      points += 1
    }
    val censusFeatures = features.filter(f => f.get("source").getOrElse("") == "census-tiger")
    if (censusFeatures.nonEmpty) {
      census += 1
    }

    if (parts.nonEmpty) {
      parsed += 1
    }
    GeocodeStats(total, parsed, points, census)
  }
}
