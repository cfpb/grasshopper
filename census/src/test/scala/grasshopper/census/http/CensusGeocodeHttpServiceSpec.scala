package grasshopper.census.http

import java.time.{ Duration, Instant }

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import grasshopper.census.model.{ CensusResult, ParsedInputAddress }
import grasshopper.census.util.TestData._
import grasshopper.elasticsearch.ElasticsearchServer
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest.{ BeforeAndAfter, FlatSpec, MustMatchers }
import spray.json._

class CensusGeocodeHttpServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with HttpService with BeforeAndAfter {
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
    Get("/") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[grasshopper.census.model.Status]
      resp.status mustBe "OK"
      resp.service mustBe "grasshopper-census"
      val statusTime = Instant.parse(resp.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

  it should "geocode an interpolated address point" in {
    val addressInput = ParsedInputAddress(
      "3146",
      "M St NW",
      "20007",
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

  it should "return BadRequest when invalid JSON is POSTed" in {
    val badJson = """{"invalid":"json"}"""
    Post("/census/addrfeat", HttpEntity(ContentTypes.`application/json`, badJson)) ~> routes ~> check(
      status mustBe BadRequest
    )
  }

  it should "return empty features array when searching for address that doesn't exist" in {
    val addressInput = ParsedInputAddress(
      "5000",
      "M St NW",
      "20007",
      "DC"
    )
    val json = addressInput.toJson.toString
    Post("/census/addrfeat", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe OK
      val resp = responseAs[CensusResult]
      resp.status mustBe "ADDRESS_NOT_FOUND"
      resp.features.size mustBe 0
    }
  }

}
