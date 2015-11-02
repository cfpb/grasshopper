package grasshopper.geocoder.http

import java.net.URLEncoder

import akka.event.{ NoLogging, LoggingAdapter }
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import grasshopper.client.parser.model.{ AddressPart, ParsedAddress }
import grasshopper.elasticsearch.ElasticsearchServer
import grasshopper.geocoder.model.{GeocodeResponse, GeocodeStatus}
import grasshopper.geocoder.util.TestData._
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.client.Client
import org.scalatest.{ FlatSpec, BeforeAndAfterAll, MustMatchers }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class HttpServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with HttpService with BeforeAndAfterAll {

  override def config: Config = testConfig
  override val logger: LoggingAdapter = NoLogging
  val server = new ElasticsearchServer
  override def client: Client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    server.createAndWaitForIndex("census")
    server.loadFeature("address", "point", getPointFeature1)
    server.loadFeature("address", "point", getPointFeature2)
    server.loadFeature("census", "addrfeat", getTigerLine1)
    client.admin().indices().refresh(new RefreshRequest("address")).actionGet()
    client.admin().indices().refresh(new RefreshRequest("census")).actionGet()
  }

  override def afterAll = {
    client.close()
    server.stop()
  }

  "The Geocoder HTTP Service" should "respond to root URL and check parser status" in {
    Get("/") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[GeocodeStatus]
      resp.parserStatus.status mustBe "OK"
    }
  }

  it should "perform a single geocode" in {
    val address = URLEncoder.encode("3146 M St NW Washington DC 20007", "UTF-8")
    val uri = Uri(s"/geocode/${address}")
    Get(uri) ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[GeocodeResponse]
      resp.query mustBe ParsedAddress("3146 M St NW Washington DC 20007",AddressPart("3146","Washington", "DC", "M St NW","20007"))
    }
  }

}
