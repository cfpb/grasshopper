package grasshopper.client.metrics

import java.net.InetAddress
import java.time.{ Duration, Instant }

import com.typesafe.scalalogging.Logger
import grasshopper.client.model.Timer
import grasshopper.client.protocol.ClientJsonProtocol
import net.logstash.logback.marker.Markers._
import org.slf4j.LoggerFactory

trait Metrics extends ClientJsonProtocol {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-client-metrics"))

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
