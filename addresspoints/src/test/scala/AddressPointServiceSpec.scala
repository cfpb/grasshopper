package grasshopper.addresspoints

import addresspoints.api.Service
import addresspoints.model
import addresspoints.model.AddressInput
import akka.event.NoLogging
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.model.MediaTypes._
import akka.http.model.StatusCodes._
import akka.http.model.{ ContentTypes, HttpEntity }
import akka.http.testkit.ScalatestRouteTest
import feature._
import geometry._
import grasshopper.elasticsearch._
import io.geojson.FeatureJsonProtocol._
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
    val props = Map("geometry" -> p, "ADDRESS" -> "1311 30th St NW Washington DC 20007")
    val schema = Schema(List(Field("geometry", GeometryType()), Field("ADDRESS", StringType())))
    Feature(schema, props)
  }

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    server.loadFeature("address", "point", getPointFeature)
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
    }
  }

  it should "return NotFound when searching for address that doesn't exist" in {
    val address = AddressInput("1311 31th St NW Washington DC 20007")
    val json = address.toJson.toString
    Post("/addresses/points", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe NotFound
    }
  }

  it should "geocode a single point" in {
    val address = AddressInput("1311 30th St NW Washington DC 20007")
    val json = address.toJson.toString
    Post("/addresses/points", HttpEntity(ContentTypes.`application/json`, json)) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val f = responseAs[Feature]
      f mustBe getPointFeature
    }
    val a = "1311+30th+St+NW+Washington+DC+20007"
    Get("/addresses/points/" + a) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val f = responseAs[Feature]
      f mustBe getPointFeature
    }
  }

}
