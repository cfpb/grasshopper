package addresspoints.protocol

import java.net.InetAddress
import java.util.Calendar

import addresspoints.model.{ AddressInput, Status }
import addresspoints.protocol.AddressPointJsonProtocol
import org.scalatest._
import spray.json._

class AddressPointJsonProtocolSpec extends FlatSpec with MustMatchers with AddressPointJsonProtocol {

  "Status" should "convert to and from JSON" in {
    val date = Calendar.getInstance().getTime().toString
    val status = Status("OK", "grasshopper-addresspoints", date, InetAddress.getLocalHost.getHostName)
    status.toJson.convertTo[Status] mustBe status
  }

  "AddressInput" should "convert to and from JSON" in {
    val addressInput = AddressInput("1311 30th St NW Washington DC 20007")
    addressInput.toJson.convertTo[AddressInput] mustBe addressInput
  }
}

