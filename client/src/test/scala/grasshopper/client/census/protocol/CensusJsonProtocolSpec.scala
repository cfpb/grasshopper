package grasshopper.client.census.protocol

import grasshopper.model.Status
import org.scalatest.{ MustMatchers, FlatSpec }
import spray.json._

class CensusJsonProtocolSpec extends FlatSpec with MustMatchers with CensusJsonProtocol {
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

  it must "serialize to JSON" in {
    val parserStatus = Status(
      "OK",
      "grasshopper-census",
      "2015-05-06T19:14:19.304850+00:00",
      "localhost"
    )

    val json = parserStatus.toJson.toString
    println(json)
    json.parseJson.convertTo[Status] mustBe parserStatus

  }
}
