package grasshopper.client.addresspoints

import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._

class AddressPointsClientSpec extends FlatSpec with MustMatchers {

  val timeout = 5.seconds

  "A 'status' request" must "return an 'OK' response" in {
    val maybeStatus = Await.result(AddressPointsClient.status, timeout)
    maybeStatus match {
      case Right(s) =>
        s.status mustBe "OK"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }


  "A 'geocode' request" must "return a valid address point for a given address string" in {
    val maybeAddress = Await.result(AddressPointsClient.geocode("108 S Main St Bentonville AR 72712"), timeout)
    maybeAddress match {
      case Right(result) =>
        result.status mustBe "OK"
        val features = result.features
        features.size mustBe 1
        val f = features(0)
        val address = f.values.getOrElse("address", "")
        address mustBe "108 S Main St Bentonville AR 72712"
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }
  }

  it must "offer candidate address results with suggest parameter in request" in {
    val maybeAddress = Await.result(AddressPointsClient.geocode("president?suggest=5"), timeout)
    maybeAddress match {
      case Right(result) =>
        result.status mustBe "OK"
        val features = result.features
        features.size mustBe 5
        val f = features(0)
        val address = f.values.getOrElse("address", "")
        address.toString.contains("President") mustBe true
      case Left(b) =>
        b.desc mustBe "503 Service Unavailable"
        fail("SERVICE_UNAVAILABLE")
    }

  }

}

