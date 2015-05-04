package tiger.protocol

import java.net.InetAddress
import java.util.Calendar
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import tiger.model.{ AddressInput, Status }
import spray.json._

class JsonProtocolSpec extends FlatSpec with MustMatchers with JsonProtocol {

  "Status" should "convert to and from JSON" in {
    val date = Calendar.getInstance().getTime().toString
    val status = Status("OK", date, InetAddress.getLocalHost.getHostName)
    status.toJson.convertTo[Status] mustBe status
  }

  "AddressInput" should "convert to and from JSON" in {
    val addressInput = AddressInput("1311 30th St NW Washington DC 20007")
    addressInput.toJson.convertTo[AddressInput] mustBe addressInput
  }
}

