package grasshopper.client.parser.model

import java.time.Instant

import grasshopper.client.model.ClientStatus

object ParserStatus extends ClientStatus {
  def empty = {
    val now = Instant.now.toString
    ParserStatus("SERVICE_UNAVAILABLE", now, "", "")
  }
}

case class ParserStatus(status: String, time: String, upSince: String, host: String) extends ClientStatus
