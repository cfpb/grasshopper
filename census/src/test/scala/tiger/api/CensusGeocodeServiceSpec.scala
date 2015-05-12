package tiger.api

import java.time.{ Duration, Instant }
import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import grasshopper.elasticsearch.ElasticsearchServer
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest.{ BeforeAndAfter, FlatSpec, MustMatchers }
import tiger.model.{ Status, ParsedAddressInput }
import tiger.util.TestData._
import spray.json._

class CensusGeocodeServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service with BeforeAndAfter {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val server = new ElasticsearchServer

  val client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    server.loadFeature("census", "addrfeat", getTigerLine1)
    client.admin().indices().refresh(new RefreshRequest("census")).actionGet()
  }

  override def afterAll = {
    server.stop()
  }

  "Tiger Address Line Service" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[tiger.model.Status]
      resp.status mustBe "OK"
      resp.service mustBe "grasshopper-census"
      val statusTime = Instant.parse(resp.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

  it should "geocode an interpolated address point" in {
    val addressInput = ParsedAddressInput(
      3146,
      "M St NW",
      20007,
      "DC"
    )
    val json = addressInput.toJson.toString
    Post("/census/addrfeat", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
    }
    Get("/census/addrfeat?number=3146&streetName=M+St+NW&zipCode=20007&state=DC") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
    }

  }

}
