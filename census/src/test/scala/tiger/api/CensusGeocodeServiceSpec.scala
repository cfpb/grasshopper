package tiger.api

import java.time.{ Duration, Instant }

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import grasshopper.elasticsearch.ElasticsearchServer
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest.{ BeforeAndAfter, FlatSpec, MustMatchers }
import tiger.model

class CensusGeocodeServiceSpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service with BeforeAndAfter {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val server = new ElasticsearchServer

  val client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    //server.loadFeature("address", "point", getPointFeature)
    //server.loadFeature("address", "point", getPointFeature1)
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
      val statusTime = Instant.parse(resp.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

}
