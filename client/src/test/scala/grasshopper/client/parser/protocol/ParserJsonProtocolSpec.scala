package grasshopper.client.parser.protocol

import org.scalatest._
import grasshopper.client.parser.model._
import spray.json._

class ParserJsonProtocolSpec extends FlatSpec with MustMatchers with ParserJsonProtocol {

  "A ParserStatus" must "deserialize from JSON" in {
    val statusStr = """
    {
      "host": "yourhost.local",
      "status": "OK",
      "time": "2015-05-06T19:14:19.304850+00:00",
      "upSince": "2015-05-06T19:08:26.568966+00:00"
    }
    """
    val parserStatus = statusStr.parseJson.convertTo[ParserStatus]
    parserStatus.host mustBe "yourhost.local"
    parserStatus.status mustBe "OK"
    parserStatus.time mustBe "2015-05-06T19:14:19.304850+00:00"
    parserStatus.upSince mustBe "2015-05-06T19:08:26.568966+00:00"
  }

  it must "serialize to JSON" in {
    val parserStatus = ParserStatus(
      "yourhost.local",
      "OK",
      "2015-05-06T19:14:19.304850+00:00",
      "2015-05-06T19:08:26.568966+00:00"
    )

    val json = parserStatus.toJson.toString
    //println(json)
    json.parseJson.convertTo[ParserStatus] mustBe parserStatus

  }

  "An AddressPart" must "deserialize from JSON" in {
    val addrPartStr = """
     {
        "addressNumber": "1311",
        "city": "washington",
        "state": "dc",
        "streetName": "30th St",
        "zip": "20007"
      }
    """
    val addrPart = addrPartStr.parseJson.convertTo[AddressPart]
    addrPart.addressNumber mustBe "1311"
    addrPart.city mustBe "washington"
    addrPart.state mustBe "dc"
    addrPart.streetName mustBe "30th St"
    addrPart.zip mustBe "20007"
  }

  it must "serialize to JSON" in {
    val addrPart = AddressPart(
      "1311",
      "washington",
      "dc",
      "30th St",
      "20007"
    )

    val json = addrPart.toJson.toString
    json.parseJson.convertTo[AddressPart] mustBe addrPart
  }

  "A ParsedAddress" must "deserialize from JSON" in {
    val addrStr = """
    {
      "input": "1311 30th St washington dc 20007",
      "parts": {
        "addressNumber": "1311",
        "city": "washington",
        "state": "dc",
        "streetName": "30th St",
        "zip": "20007"
      }
    } 
    """
    val parsedAddress = addrStr.parseJson.convertTo[ParsedAddress]
    parsedAddress.input mustBe "1311 30th St washington dc 20007"
    parsedAddress.parts.addressNumber mustBe "1311"
    parsedAddress.parts.city mustBe "washington"
    parsedAddress.parts.state mustBe "dc"
    parsedAddress.parts.streetName mustBe "30th St"
    parsedAddress.parts.zip mustBe "20007"

  }

  it must "serialize to JSON" in {
    val addrPart = AddressPart(
      "1311",
      "washington",
      "dc",
      "30th St",
      "20007"
    )
    val inputStr = "1311 30th St washington dc 20007"
    val parsedAddress = ParsedAddress(inputStr, addrPart)
    val json = parsedAddress.toJson.toString
    json.parseJson.convertTo[ParsedAddress] mustBe parsedAddress
  }

}
