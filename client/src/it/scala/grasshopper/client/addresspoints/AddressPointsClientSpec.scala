package grasshopper.client.addresspoints

import grasshopper.client.addresspoints.AddressPointsClient
import grasshopper.client.addresspoints.model.AddressPointsStatus
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class AddressPointsJsonProtocolSpec extends FlatSpec with MustMatchers {

 "A request to /status" must "return a status object" in {
    val maybeStatus: Either[String, AddressPointsStatus] = Await.result(AddressPointsClient.status, 10.seconds)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(_) =>
        fail("The call to /status failed")
    }
  }
 
  it must "geocode an address string" in {
    val maybeAddress = Await.result(AddressPointsClient.geocode("108+S+Main+St+Bentonville+AR+72712"), 10.seconds)
    maybeAddress match {
      case Right(features) =>
        features.size mustBe 1
        val f = features(0)
        val address = f.values.getOrElse("address", "")
        address mustBe "108 S Main St Bentonville AR 72712"
      case Left(_) =>
        fail("The call to /geocode failed")
    }
  }

  it must "offer candidate address results with suggest parameter in request" in {
 val maybeAddress = Await.result(AddressPointsClient.geocode("president?suggest=5"), 10.seconds)
    maybeAddress match {
      case Right(features) =>
        features.size mustBe 5
        val f = features(0)
        val address = f.values.getOrElse("address", "")
        address mustBe "701 President St Arkansas City AR 71630"
      case Left(_) =>
        fail("The call to /geocode failed")
    } 
  
  }


}

