package grasshopper.addresspoints

import java.net.InetAddress

import addresspoints.model.{ Status, AddressInput }
import addresspoints.protocol.JsonProtocol
import org.scalatest._
import spray.json._
import java.util.Calendar

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

