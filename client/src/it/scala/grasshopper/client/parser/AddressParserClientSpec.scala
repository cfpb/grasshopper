package grasshopper.client.parser

import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class AddressParserClientSpec extends FlatSpec with MustMatchers {

  val timeout = 5.seconds

  "A 'status' request" must "return an 'OK' response" in {
    val maybeStatus = Await.result(AddressParserClient.status, timeout)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }

  "A 'parse' request" must "parse an address string into its component parts" in {
    val maybeAddress = Await.result(AddressParserClient.parse("1311 30th St NW washington dc 20007"), timeout)
    maybeAddress match {
      case Right(parsed) =>
        parsed.parts.size mustBe 9
        parsed.parts(0).`type` mustBe "address_number"
        parsed.parts(0).value mustBe "1311"
        parsed.parts(1).`type` mustBe "street_name"
        parsed.parts(1).value mustBe "30th"
        parsed.parts(2).`type` mustBe "street_name_post_type"
        parsed.parts(2).value mustBe "St"
        parsed.parts(3).`type` mustBe "street_name_post_directional"
        parsed.parts(3).value mustBe "NW"
        parsed.parts(4).`type` mustBe "city_name"
        parsed.parts(4).value mustBe "washington"
        parsed.parts(5).`type` mustBe "state_name"
        parsed.parts(5).value mustBe "dc"
        parsed.parts(6).`type` mustBe "zip_code"
        parsed.parts(6).value mustBe "20007"
        parsed.parts(7).`type` mustBe "address_number_full"
        parsed.parts(7).value mustBe "1311"
        parsed.parts(8).`type` mustBe "street_name_full"
        parsed.parts(8).value mustBe "30th St NW"

      case Left(failed) =>
        failed.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }
}
