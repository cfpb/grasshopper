package grasshopper.client.parser

import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class AddressParserClientSpec extends FlatSpec with MustMatchers {

  "A request to /status" must "return a status object" in {
    val maybeStatus = Await.result(AddressParserClient.status, 1.seconds)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
    }
  }

  "A request to /parse" must "parse an address string" in {
    val maybeAddress = Await.result(AddressParserClient.parse("1311+30th+st+nw+washington+dc+20007"), 2.seconds)
    maybeAddress match {
      case Right(a) =>
        a.parts.AddressNumber mustBe "1311"
        a.parts.PlaceName mustBe "washington"
        a.parts.StateName mustBe "dc"
        a.parts.StreetName mustBe "30th"
        a.parts.StreetNamePostType mustBe "st"
        a.parts.ZipCode mustBe "20007"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
    }
  }
}
