package grasshopper.client.parser

import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class AddressParserClientSpec extends FlatSpec with MustMatchers {

  "A 'status' request" must "return an 'OK' response" in {
    val maybeStatus = Await.result(AddressParserClient.status, 1.seconds)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }

  "A 'standardize' request" must "parse an address string into its component parts" in {
    val maybeAddress = Await.result(AddressParserClient.standardize("1311 30th St NW washington dc 20007"), 2.seconds)
    maybeAddress match {
      case Right(a) =>
        a.parts.addressNumber mustBe "1311"
        a.parts.city mustBe "washington"
        a.parts.state mustBe "dc"
        a.parts.streetName mustBe "30th St NW"
        a.parts.zip mustBe "20007"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }
}
