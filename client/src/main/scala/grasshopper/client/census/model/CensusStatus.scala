package grasshopper.client.census.model

import java.time.Instant

import grasshopper.client.model.ClientStatus

object CensusStatus extends ClientStatus {
  def empty = {
    val now = Instant.now.toString
    CensusStatus("SERVICE_UNAVAILABLE", "grasshopper-census", now, "")
  }
}

case class CensusStatus(status: String, service: String, time: String, host: String)
