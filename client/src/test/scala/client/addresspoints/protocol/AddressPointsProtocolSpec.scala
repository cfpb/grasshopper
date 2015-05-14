package grasshopper.client.addresspoints.protocol

import org.scalatest._
import grasshopper.client.addresspoints.model._
import spray.json._

class AddressPointsJsonProtocolSpec extends FlatSpec with MustMatchers with AddressPointsJsonProtocol {

  "An AddressStatus" must "deserialize from JSON" in {
    val statusStr = """
    {
      "host": "yourhost.local",
      "status": "OK",
      "time": "2015-05-06T19:14:19.304850+00:00",
      "service": "grasshopper-addresspoints"
    }
    """
    val addressStatus = statusStr.parseJson.convertTo[AddressPointsStatus]
    addressStatus.host mustBe "yourhost.local"
    addressStatus.status mustBe "OK"
    addressStatus.time mustBe "2015-05-06T19:14:19.304850+00:00"
    addressStatus.service mustBe "grasshopper-addresspoints"
  }

  it must "serialize to JSON" in {
    val parserStatus = AddressPointsStatus(
      "OK",
      "grasshopper-addresspoints",
      "2015-05-06T19:14:19.304850+00:00",
      "localhost"
    )

    val json = parserStatus.toJson.toString
    println(json)
    json.parseJson.convertTo[AddressPointsStatus] mustBe parserStatus

  }

}
