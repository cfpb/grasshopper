package grasshopper.geocoder.api

import java.time.{ Duration, Instant }

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import grasshopper.geocoder.model.GeocodeStatus
import org.scalatest.{ BeforeAndAfter, FlatSpec, MustMatchers }

class GrasshopperServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service with BeforeAndAfter {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "Geocoder Service" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[GeocodeStatus]

      resp.addressPointsStatus.service mustBe "grasshopper-addresspoints"
      resp.censusStatus.service mustBe "grasshopper-census"

      val statusTime = Instant.parse(resp.addressPointsStatus.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

}
