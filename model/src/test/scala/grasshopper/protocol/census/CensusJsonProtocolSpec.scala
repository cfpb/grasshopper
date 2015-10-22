package grasshopper.protocol.census

import java.net.InetAddress
import java.util.Calendar

import grasshopper.model.Status
import grasshopper.model.census.ParsedInputAddress
import grasshopper.protocol.StatusJsonProtocol
import org.scalatest.{ FlatSpec, MustMatchers }
import spray.json._

class CensusJsonProtocolSpec extends FlatSpec with MustMatchers with StatusJsonProtocol with CensusJsonProtocol {

  "A CensusStatus" must "deserialize from JSON" in {
    val statusStr = """
    {
      "host": "yourhost.local",
      "status": "OK",
      "time": "2015-05-06T19:14:19.304850+00:00",
      "service": "grasshopper-census"
    }
                    """
    val addressStatus = statusStr.parseJson.convertTo[Status]
    addressStatus.host mustBe "yourhost.local"
    addressStatus.status mustBe "OK"
    addressStatus.time mustBe "2015-05-06T19:14:19.304850+00:00"
    addressStatus.service mustBe "grasshopper-census"
  }

  "Status" should "convert to and from JSON" in {
    val date = Calendar.getInstance().getTime().toString
    val status = Status("OK", "grasshopper-census", date, InetAddress.getLocalHost.getHostName)
    status.toJson.convertTo[Status] mustBe status
  }

  "ParsedAddressInput" should "convert to and from JSON" in {
    val addressInput = ParsedInputAddress("1311", "30th St NW", "20007", "DC")
    addressInput.toJson.convertTo[ParsedInputAddress] mustBe addressInput
  }
}

