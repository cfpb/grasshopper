package grasshopper.addresspoints

import akka.event.NoLogging
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.model.{ HttpResponse, HttpRequest, HttpMethods, HttpEntity, ContentTypes }
import akka.http.model.headers._
import akka.http.model.MediaTypes._
import akka.http.model.StatusCodes._
import akka.http.testkit.ScalatestRouteTest
import akka.util.ByteString
import akka.stream.scaladsl._
import org.scalatest._
import grasshopper.elasticsearch._
import grasshopper.geometry._
import grasshopper.feature._
import spray.json._

class AddressPointServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service with BeforeAndAfter {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val server = new ElasticsearchServer

  override val client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    val p = Point(-77.0590232, 38.9072597)
    val properties = Map("ADDRESS" -> "1311 30th St NW Washington DC 20007")
    val f = Feature(p, properties)
    server.loadFeature("address", "point", f)
  }

  override def afterAll = {
    server.stop()
  }

  "Address Point Service" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[Status]
      resp.status mustBe "OK"
    }
  }

  it should "geocode a single point" in {
    val address = AddressInput(1, "1311 30th St NW Washington DC 20007")
    val json = address.toJson.toString
    //val json = """{"id":1,"address":"1311 30th St NW Washington DC 20007"}"""
    Post("/address/point", HttpEntity(ContentTypes.`application/json`, ByteString(json))) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
    }
  }

}
