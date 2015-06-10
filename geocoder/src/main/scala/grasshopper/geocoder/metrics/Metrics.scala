package grasshopper.geocoder.metrics

import java.time.{ Duration, Instant }
import com.typesafe.scalalogging.Logger
import grasshopper.geocoder.protocol.GrasshopperJsonProtocol
import kamon.Kamon
import org.slf4j.LoggerFactory

trait Metrics extends GrasshopperJsonProtocol {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-geocoder-metrics"))

  def time[T](name: String, f: => T): T = {
    val counter = Kamon.metrics.counter(name)
    counter.increment()
    val timerHist = Kamon.metrics.histogram(name)
    val start = Instant.now
    val result = f
    val end = Instant.now
    val duration = Duration.between(start, end)
    timerHist.record(duration.toMillis)
    result
  }
}
