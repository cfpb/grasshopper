package grasshopper.geocoder.api.stats

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.config.ConfigFactory
import feature.{ Feature, FeatureCollection }
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStats }
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol

import scala.collection.mutable
import scala.concurrent.duration._

object GeocodeStatsAggregator {
  case class PublishStats()
  def props: Props = Props(new GeocodeStatsAggregator)
}

class GeocodeStatsAggregator extends Actor with ActorLogging with GrasshopperJsonProtocol {
  import grasshopper.geocoder.api.stats.GeocodeStatsAggregator._

  import scala.concurrent.ExecutionContext.Implicits.global

  var total = 0
  var parsed = 0
  var geocoded = 0
  var points = 0
  var census = 0
  var featureQueue = mutable.Queue[Feature]()

  var stats = GeocodeStats(total, parsed, geocoded, points, census, FeatureCollection(featureQueue.toList))

  val config = ConfigFactory.load()

  val delay = config.getInt("grasshopper.geocoder.metrics.delay").seconds
  val interval = config.getInt("grasshopper.geocoder.metrics.interval").milliseconds

  // publish stats every x ms to the event stream
  context.system.scheduler.schedule(delay, interval, self, PublishStats)

  override def receive: Receive = {
    case g: GeocodeResponse =>
      computeGeocodeStats(g)
      stats = GeocodeStats(total, parsed, points, census, geocoded, FeatureCollection(featureQueue.toList))
    case PublishStats =>
      context.system.eventStream.publish(stats)
    case _ => //ignore all other messages
      log.warning("Message not supported")
  }

  private def computeGeocodeStats(g: GeocodeResponse): GeocodeStats = {
    total += 1
    val parts = g.parts
    val features = g.features

    val pointFeatures = features.filter(f => f.get("source").getOrElse("") == "state-address-points")
    if (pointFeatures.nonEmpty) {
      points += 1
      featureQueue.enqueue(pointFeatures.head)
    }
    val censusFeatures = features.filter(f => f.get("source").getOrElse("") == "census-tiger")

    if (censusFeatures.nonEmpty) {
      census += 1
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
