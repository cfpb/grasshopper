package grasshopper.protocol.census

import java.net.InetAddress
import java.util.Calendar

import grasshopper.model.{ SearchableAddress, Status }
import grasshopper.protocol.{ AddressSearchJsonProtocol, StatusJsonProtocol }
import org.scalatest.{ FlatSpec, MustMatchers }
import spray.json._

class AddressSearchJsonProtocolSpec extends FlatSpec with MustMatchers with StatusJsonProtocol with AddressSearchJsonProtocol {

  "A Status" must "deserialize from JSON" in {
    val statusStr = """
    {
      "host": "yourhost.local",
      "status": "OK",
      "time": "2015-05-06T19:14:19.304850+00:00",
      "service": "grasshopper-census"
    }
                    """
    val status = statusStr.parseJson.convertTo[Status]
    status.host mustBe "yourhost.local"
    status.status mustBe "OK"
    status.time mustBe "2015-05-06T19:14:19.304850+00:00"
    status.service mustBe "grasshopper-census"
  }

  "Status" should "convert to and from JSON" in {
    val date = Calendar.getInstance().getTime().toString
    val status = Status("OK", "grasshopper-service", date, InetAddress.getLocalHost.getHostName)
    status.toJson.convertTo[Status] mustBe status
  }

}
