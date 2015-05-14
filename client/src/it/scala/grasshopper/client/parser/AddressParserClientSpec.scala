package grasshopper.client.parser

import grasshopper.client.parser.model.ParserStatus
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

class AddressParserClientSpec extends FlatSpec with MustMatchers {

  "A request to /status" must "return a status object" in {
    val maybeStatus: Either[String, ParserStatus] = Await.result(AddressParserClient.status, 10.seconds)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(_) =>
        fail("The call to /status failed")
    }
  }

  it must "parse an address string" in {
    val maybeAddress = Await.result(AddressParserClient.parse("1311+30th+st+nw+washington+dc+20007"), 10.seconds)
    maybeAddress match {
      case Right(a) =>
        a.parts.AddressNumber mustBe "1311"
        a.parts.PlaceName mustBe "washington"
        a.parts.StateName mustBe "dc"
        a.parts.StreetName mustBe "30th"
        a.parts.StreetNamePostType mustBe "st"
        a.parts.ZipCode mustBe "20007"
      case Left(_) =>
        fail("The call to /parse failed")
    }
  }
}
