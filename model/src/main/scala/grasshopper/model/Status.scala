package grasshopper.model

import java.time.Instant

trait ServiceStatus

case class Status(status: String, service: String, time: String, host: String) extends ServiceStatus

object Status extends ServiceStatus {
  def empty = {
    val now = Instant.now.toString
    Status("SERVICE_UNAVAILABLE", "", now, "")
  }
}