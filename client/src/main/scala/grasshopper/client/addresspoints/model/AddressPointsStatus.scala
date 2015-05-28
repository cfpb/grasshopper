package grasshopper.client.addresspoints.model

import java.time.Instant

import grasshopper.client.model.ClientStatus

object AddressPointsStatus extends ClientStatus {
  def empty = {
    val now = Instant.now.toString
    AddressPointsStatus("SERVICE_UNAVAILABLE", "grasshopper-addresspoints", now, "")
  }
}

case class AddressPointsStatus(status: String, service: String, time: String, host: String)
