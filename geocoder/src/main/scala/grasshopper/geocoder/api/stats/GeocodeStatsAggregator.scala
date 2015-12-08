package grasshopper.geocoder.api.stats

import akka.actor.{ Props, ActorLogging, Actor }
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import grasshopper.geocoder.model.GeocodeStats
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import spray.json._

object GeocodeStatsAggregator {
  case class PublishStats()
  def props: Props = Props(new GeocodeStatsAggregator)
}

class GeocodeStatsAggregator extends Actor with ActorLogging with GrasshopperJsonProtocol {
  import grasshopper.geocoder.api.stats.GeocodeStatsAggregator._
  import scala.concurrent.ExecutionContext.Implicits.global

  var stats = GeocodeStats(0, 0, 0, 0)

  val config = ConfigFactory.load()

  val delay = config.getInt("grasshopper.geocoder.metrics.delay").seconds
  val interval = config.getInt("grasshopper.geocoder.metrics.interval").milliseconds

  // publish stats every x ms to the event stream
  context.system.scheduler.schedule(delay, interval, self, PublishStats)

  override def receive: Receive = {
    case g: GeocodeStats =>
      val aggrStats = calculateStats(g)
      stats = aggrStats
      log.info(stats.toJson.toString)
    case PublishStats =>
      context.system.eventStream.publish(stats)
    case _ => //ignore all other messages
  }

  private def calculateStats(g: GeocodeStats): GeocodeStats = {
    val total = stats.total + g.total
    val parsed = stats.parsed + g.parsed
    val points = stats.points + g.points
    val census = stats.census + g.census
    GeocodeStats(total, parsed, points, census)
  }
}
