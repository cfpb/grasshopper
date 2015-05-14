package grasshopper.client.census

import grasshopper.client.census.model.{ParsedInputAddress, CensusStatus}
import org.scalatest.{MustMatchers, FlatSpec}
import scala.concurrent.Await
import scala.concurrent.duration._

class CensusClientSpec extends FlatSpec with MustMatchers {
  "A request to /status" must "return a status object" in {
    val maybeStatus: Either[String, CensusStatus] = Await.result(CensusClient.status, 10.seconds)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(_) =>
        fail("The call to /status failed")
    }
  }

  it must "geocode an address string" in {
    val parsedAddress = ParsedInputAddress(1311, "30th+St+NW", 20007, "DC")
    val maybeAddress = Await.result(CensusClient.geocode(parsedAddress), 10.seconds)
    maybeAddress match {
      case Right(features) =>
        features.size mustBe 1
        val f = features(0)
        val address = f.values.getOrElse("FULLNAME", "")
        address mustBe "30th St NW"
      case Left(_) =>
        fail("The call to /geocode failed")
    }
  }

}
