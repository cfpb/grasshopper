package grasshopper.client.addresspoints.protocol

import grasshopper.model.Status
import grasshopper.protocol.StatusJsonProtocol
import grasshopper.protocol.addresspoints.AddressPointsJsonProtocol
import org.scalatest._
import spray.json._

class AddressPointsClientJsonProtocolSpec extends FlatSpec with MustMatchers with StatusJsonProtocol with AddressPointsJsonProtocol {

  it must "serialize to JSON" in {
    val parserStatus = Status(
      "OK",
      "grasshopper-addresspoints",
      "2015-05-06T19:14:19.304850+00:00",
      "localhost"
    )

    val json = parserStatus.toJson.toString
    println(json)
    json.parseJson.convertTo[Status] mustBe parserStatus

  }

}
