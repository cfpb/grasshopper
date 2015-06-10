package grasshopper.census.metrics

import java.net.InetAddress
import java.time.{ Duration, Instant }
import com.typesafe.scalalogging.Logger
import grasshopper.census.model.Timer
import grasshopper.census.protocol.CensusJsonProtocol
import org.slf4j.LoggerFactory
import net.logstash.logback.marker.Markers._
import spray.json._

trait Metrics extends CensusJsonProtocol {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census-metrics"))

  def time[T](name: String, f: => T): T = {
    val start = Instant.now
    val result = f
    val end = Instant.now
    val duration = Duration.between(start, end)
    val host = InetAddress.getLocalHost
    val timer = Timer(duration.toMillis)
    log.info(append(name, timer.duration.toString()), timer.duration.toString)
    result
  }
}