package grasshopper.geocoder.api.stats

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorSubscriber, RequestStrategy, WatermarkRequestStrategy }
import feature.{ FeatureCollection, Feature }
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStats }
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol

import scala.collection.mutable

object GeocodeStatsSubscriber {
  def props: Props = Props(new GeocodeStatsSubscriber)
}

class GeocodeStatsSubscriber extends ActorSubscriber with ActorLogging with GrasshopperJsonProtocol {

  var total: Int = 0
  var parsed: Int = 0
  var points: Int = 0
  var census: Int = 0
  var geocoded: Int = 0
  var featureQueue = mutable.Queue[Feature]()

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
      log.debug("Geocoding Stream completed")
  }

  private def computeGeocodeStats(g: GeocodeResponse): GeocodeStats = {
    total += 1
    val parts = g.parts
    val features = g.features

    def removeFromFeatureQueue(): Unit = {
      if (features.size >= 100) {
        val removed = featureQueue.reverse.dequeue
        log.info(s"Element removed: ${removed.toString}")
      }
    }

    val pointFeatures = features.filter(f => f.get("source").getOrElse("") == "state-address-points")
    if (pointFeatures.nonEmpty) {
      points += 1
      removeFromFeatureQueue()
      featureQueue.enqueue(pointFeatures.head)
    }
    val censusFeatures = features.filter(f => f.get("source").getOrElse("") == "census-tiger")

    if (censusFeatures.nonEmpty) {
      census += 1
      removeFromFeatureQueue()
      featureQueue.enqueue(censusFeatures.head)
    }

    if (pointFeatures.nonEmpty || censusFeatures.nonEmpty) {
      geocoded += 1
    }

    if (parts.nonEmpty) {
      parsed += 1
    }

    GeocodeStats(total, parsed, points, census, geocoded, FeatureCollection(featureQueue.toList))
  }
}
