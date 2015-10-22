package grasshopper.client.census.protocol

import grasshopper.model.Status
import grasshopper.protocol.StatusJsonProtocol
import org.scalatest.{ MustMatchers, FlatSpec }
import spray.json._

class CensusJsonProtocolSpec extends FlatSpec with MustMatchers with StatusJsonProtocol {

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
