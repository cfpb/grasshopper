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
        "AddressNumber": "1311",
        "PlaceName": "washington",
        "StateName": "dc",
        "StreetName": "30th",
        "StreetNamePostType": "st",
        "ZipCode": "20007"
      }
    """
    val addrPart = addrPartStr.parseJson.convertTo[AddressPart]
    addrPart.AddressNumber mustBe "1311"
    addrPart.PlaceName mustBe "washington"
    addrPart.StateName mustBe "dc"
    addrPart.StreetName mustBe "30th"
    addrPart.StreetNamePostType mustBe "st"
    addrPart.ZipCode mustBe "20007"
  }

  it must "serialize to JSON" in {
    val addrPart = AddressPart(
      "1311",
      "washington",
      "dc",
      "30th",
      "st",
      "20007"
    )

    val json = addrPart.toJson.toString
    json.parseJson.convertTo[AddressPart] mustBe addrPart
  }

  "A ParsedAddress" must "deserialize from JSON" in {
    val addrStr = """
    {
      "input": "1311 30th st washington dc 20007",
      "parts": {
        "AddressNumber": "1311",
        "PlaceName": "washington",
        "StateName": "dc",
        "StreetName": "30th",
        "StreetNamePostType": "st",
        "ZipCode": "20007"
      }
    } 
    """
    val parsedAddress = addrStr.parseJson.convertTo[ParsedAddress]
    parsedAddress.input mustBe "1311 30th st washington dc 20007"
    parsedAddress.parts.AddressNumber mustBe "1311"
    parsedAddress.parts.PlaceName mustBe "washington"
    parsedAddress.parts.StateName mustBe "dc"
    parsedAddress.parts.StreetName mustBe "30th"
    parsedAddress.parts.StreetNamePostType mustBe "st"
    parsedAddress.parts.ZipCode mustBe "20007"

  }

  it must "serialize to JSON" in {
    val addrPart = AddressPart(
      "1311",
      "washington",
      "dc",
      "30th",
      "st",
      "20007"
    )
    val inputStr = "1311 30th st washington dc 20007"
    val parsedAddress = ParsedAddress(inputStr, addrPart)
    val json = parsedAddress.toJson.toString
    println(json)
    json.parseJson.convertTo[ParsedAddress] mustBe parsedAddress
  }

}
