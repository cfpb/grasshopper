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
    json.parseJson.convertTo[ParserStatus] mustBe parserStatus

  }

  "An AddressPart" must "deserialize from JSON" in {
    val addrPartStr = """{ "code": "address_number", "value": "1311"}"""

    val addrPart = addrPartStr.parseJson.convertTo[AddressPart]
    addrPart.code mustBe "address_number"
    addrPart.value mustBe "1311"
  }

  it must "serialize to JSON" in {
    val addrPart = AddressPart("address_number", "1311")

    val json = addrPart.toJson.toString
    json.parseJson.convertTo[AddressPart] mustBe addrPart
  }

  "A ParsedAddress" must "deserialize from JSON" in {
    val addrStr = """
    {
      "input": "1311 30th St washington dc 20007",
      "parts": [
        {"code": "address_number", "value": "1311"},
        {"code": "street_name", "value": "30th St"},
        {"code": "city_name", "value": "washington"},
        {"code": "state_name", "value": "dc"},
        {"code": "zip_code", "value": "20007"}
      ]
    } 
    """
    val parsed = addrStr.parseJson.convertTo[ParsedAddress]
    parsed.parts(0).code mustBe "address_number"
    parsed.parts(0).value mustBe "1311"
    parsed.parts(1).code mustBe "street_name"
    parsed.parts(1).value mustBe "30th St"
    parsed.parts(2).code mustBe "city_name"
    parsed.parts(2).value mustBe "washington"
    parsed.parts(3).code mustBe "state_name"
    parsed.parts(3).value mustBe "dc"
    parsed.parts(4).code mustBe "zip_code"
    parsed.parts(4).value mustBe "20007"
  }

  it must "serialize to JSON" in {
    val parsedAddress = ParsedAddress("1311 30th St washington dc 20007", List(
      AddressPart("address_number", "1311"),
      AddressPart("street_number", "30th St"),
      AddressPart("city_name", "washington"),
      AddressPart("state_name", "dc"),
      AddressPart("zip_code", "20007")
    ))

    val json = parsedAddress.toJson.toString
    json.parseJson.convertTo[ParsedAddress].parts mustBe parsedAddress.parts
  }

}
