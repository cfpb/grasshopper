import akka.event.NoLogging
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.model.ContentTypes._
import akka.http.model.{ HttpResponse, HttpRequest }
import akka.http.model.StatusCodes._
import akka.http.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._
import grasshopper.elasticsearch._

class AddressPointServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val es = new ElasticsearchServer

  "Address Point Service" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType mustBe `application/json`
      val resp = responseAs[Status]
      resp.status mustBe "OK"
    }
  }

  it should "geocode a single point" in {
    Post("/address/point", AddressInput(1, "1311 30th St NW Washington DC 20007")) ~> routes ~> check {
      status mustBe OK
      contentType mustBe `application/json`
    }
  }

}
