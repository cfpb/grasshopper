package grasshopper.addresspoints.api

import java.time.{ Duration, Instant }

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import feature._
import geometry._
import grasshopper.addresspoints.model
import grasshopper.addresspoints.model.{ AddressInput, AddressPointsResult }
import grasshopper.elasticsearch._
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest._
import spray.json._

class AddressPointServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service with BeforeAndAfter {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val server = new ElasticsearchServer

  val client = server.client

  private def getPointFeature() = {
    val p = Point(-77.0590232, 38.9072597)
    val props = Map("geometry" -> p, "address" -> "1311 30th St NW Washington DC 20007", "match" -> 1)
    val schema = Schema(List(Field("geometry", GeometryType()), Field("address", StringType()), Field("match", DoubleType())))
    Feature(schema, props)
  }

  private def getPointFeature1() = {
    val p = Point(-77.059017, 38.907271)
    val props = Map("geometry" -> p, "address" -> "1315 30th St NW Washington DC 20007")
    val schema = Schema(List(Field("geometry", GeometryType()), Field("address", StringType())))
    Feature(schema, props)
  }

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    server.loadFeature("address", "point", getPointFeature)
    server.loadFeature("address", "point", getPointFeature1)
    client.admin().indices().refresh(new RefreshRequest("address")).actionGet()
  }

  override def afterAll = {
    server.stop()
  }

  "Address Point Service" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[model.Status]

      resp.status mustBe "OK"
      resp.service mustBe "grasshopper-addresspoints"

      val statusTime = Instant.parse(resp.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

  it should "return empty features array when searching for address that doesn't exist" in {
    val address = AddressInput("228 Park Ave S New York NY 10003")
    val json = address.toJson.toString
    Post("/addresses/points", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe OK
      val resp = responseAs[AddressPointsResult]
      resp.status mustBe "ADDRESS_NOT_FOUND"
      resp.features.size mustBe 0
    }
  }

  it should "return BadRequest when invalid JSON is POSTed" in {
    val badJson = """{"invalid":"json"}"""
    Post("/addresses/points", HttpEntity(ContentTypes.`application/json`, badJson)) ~> routes ~> check(
      status mustBe BadRequest
    )
  }

  it should "geocode a single point" in {
    val address = AddressInput("1311 30th St NW Washington DC 20007")
    val json = address.toJson.toString
    Post("/addresses/points", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[AddressPointsResult]
      resp.features(0) mustBe getPointFeature()
    }
    val a = "1311+30th+St+NW+Washington+DC+20007"
    Get("/addresses/points/" + a) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[AddressPointsResult]
      resp.features(0) mustBe getPointFeature
    }
  }

  it should "suggest a few addresses" in {
    val a = "30th+St+NW"
    Get("/addresses/points/" + a + "?suggest=2") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[AddressPointsResult]
      resp.features.size mustBe 2
    }
  }

}
