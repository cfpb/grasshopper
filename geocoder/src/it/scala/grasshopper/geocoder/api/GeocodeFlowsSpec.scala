package grasshopper.geocoder.api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import geometry.Point
import grasshopper.client.parser.model.{AddressPart, ParsedAddress}
import grasshopper.elasticsearch.ElasticsearchServer
import grasshopper.model.census.ParsedInputAddress
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}
import grasshopper.geocoder.util.TestData._

import scala.concurrent.Await
import scala.concurrent.duration._

class GeocodeFlowsSpec extends FlatSpec with MustMatchers with GeocodeFlow with BeforeAndAfterAll {

  implicit val system = ActorSystem("sys")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val server = new ElasticsearchServer

  val client = server.client

  val timeout = 5.seconds

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("address")
    server.createAndWaitForIndex("census")
    server.loadFeature("address", "point", getPointFeature1)
    server.loadFeature("census", "addrfeat", getTigerLine1)
    client.admin().indices().refresh(new RefreshRequest("address")).actionGet()
    client.admin().indices().refresh(new RefreshRequest("census")).actionGet()
  }

  override def afterAll = {
    client.close()
    server.stop()
  }

  "GeocodeFlows" must "parse a list of addresses" in {
    val addresses =
      List(
        "1311 30th St NW Washington DC 20007",
        "3146 M St NW Washington DC 20007",
        "198 President St Arkansas City AR 71630",
        "1 Main St City ST 00001"
      ).toIterator

    val source = Source(() => addresses)

    val future = source.via(parseFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 4
    result.head.input mustBe "1311 30th St NW Washington DC 20007"
    result.head.parts.addressNumber mustBe "1311"
    result.head.parts.city mustBe "Washington"
    result.head.parts.state mustBe "DC"
    result.head.parts.zip mustBe "20007"

    result.tail.head.input mustBe "3146 M St NW Washington DC 20007"
    result.tail.head.parts.addressNumber mustBe "3146"
    result.tail.head.parts.city mustBe "Washington"
    result.tail.head.parts.state mustBe "DC"
    result.tail.head.parts.zip mustBe "20007"
  }

  it must "transform parsed addresses into parsed input addresses" in {
    val inputParsedList = List(
      ParsedAddress("1311 30th St NW Washington DC 20007",
        AddressPart("1311","Washington","DC","30th St NW","20007")),
      ParsedAddress("3146 M St NW Washington DC 20007",
        AddressPart("3146","Washington","DC","M St NW","20007")),
      ParsedAddress("198 President St Arkansas City AR 71630",
        AddressPart("198","Arkansas City","AR","President St","71630")),
      ParsedAddress("1 Main St City ST 00001",AddressPart("1","St City","ST","Main","00001")))

    val source = Source(() => inputParsedList.toIterator)
    val future = source.via(parsedInputAddressFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 4
    result.head.toString() mustBe "1311 30th St NW Washington DC 20007"
    result.head mustBe ParsedInputAddress("1311", "30th St NW", "Washington", "20007", "DC")

    result.tail.head.toString() mustBe "3146 M St NW Washington DC 20007"
    result.tail.head mustBe ParsedInputAddress("3146", "M St NW", "Washington", "20007", "DC")

  }

  it must "geocode with State Address Points single field" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source(() => addresses)

    val future = source.via(geocodePointFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 1
    result.head.geometry.centroid.roundCoordinates(3) mustBe Point(-77.062, 38.905)
  }

  it must "geocode with all State Address Points fields" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source(() => addresses)
    val futureParsed = source.via(parseFlow).grouped(4).runWith(Sink.head)
    val resultParsed = Await.result(futureParsed, timeout)

    resultParsed.size mustBe 1

    val sourceParsed = Source (() =>List(resultParsed.head).toIterator)

    val future = sourceParsed.via(geocodePointFieldsFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    println(result)
    result.size mustBe 1
    result.head.geometry.centroid.roundCoordinates(3) mustBe Point(-77.062, 38.905)
  }

  it must "geocode with TIGER line" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source(() => addresses)

    val future = source
      .via(parseFlow)
      .via(parsedInputAddressFlow)
      .via(geocodeLineFlow)
      .grouped(1)
      .runWith(Sink.head)

    val result = Await.result(future, timeout)
    result.size mustBe 1
    result.head.geometry.centroid.roundCoordinates(3) mustBe Point(-77.062, 38.905)
  }

  it must "perform cascade geocode" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source(() => addresses)

    val future = source
      .via(geocodeFlow)
      .grouped(1)
      .runWith(Sink.head)

    val result = Await.result(future, timeout)
    result.size mustBe 1
    result.head.features.size mustBe 2
  }

  it must "choose address point result when batch geocoding" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source(() => addresses)

    val future = source
      .via(geocodeFlow)
      .via(filterPointResultFlow)
      .grouped(1)
      .runWith(Sink.head)

    val result = Await.result(future, timeout)
    result.size mustBe 1
    result.head.geometry mustBe getPointFeature1.geometry
  }

}
