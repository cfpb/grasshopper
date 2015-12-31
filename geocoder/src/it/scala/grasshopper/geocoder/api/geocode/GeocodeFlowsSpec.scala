package grasshopper.geocoder.api.geocode

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import geometry.Point
import grasshopper.client.parser.model.{AddressPart, ParsedAddress}
import grasshopper.elasticsearch.ElasticsearchServer
import grasshopper.geocoder.util.TestData._
import grasshopper.model.SearchableAddress
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.scalatest.{BeforeAndAfterAll, FlatSpec, MustMatchers}

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
        "198 President St, Arkansas City AR 71630"
      ).toIterator

    val source = Source.fromIterator(() => addresses)

    val future = source.via(parseFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 3

    val result1 = result(0)
    result1.input mustBe "1311 30th St NW Washington DC 20007"
    result1.parts.length mustBe 9
    result1.parts(7) mustBe AddressPart("address_number_full", "1311")
    result1.parts(8) mustBe AddressPart("street_name_full", "30th St NW")

    val result2 = result(1)
    result2.input mustBe "3146 M St NW Washington DC 20007"
    result2.parts.length mustBe 9
    result2.parts(7) mustBe AddressPart("address_number_full", "3146")
    result2.parts(8) mustBe AddressPart("street_name_full", "M St NW")

    val result3 = result(2)
    result3.input mustBe "198 President St, Arkansas City AR 71630"
    result3.parts.length mustBe 8
    result3.parts(6) mustBe AddressPart("address_number_full", "198")
    result3.parts(7) mustBe AddressPart("street_name_full", "President St")
  }

  it must "transform parsed addresses into parsed searchable addresses" in {
    val inputParsedList = List(
      ParsedAddress("1311 30th St NW Washington DC 20007", List(
        AddressPart("address_number_full", "1311"),
        AddressPart("street_name_full", "30th St NW"),
        AddressPart("city_name", "Washington"),
        AddressPart("state_name", "DC"),
        AddressPart("zip_code","20007"))),
      ParsedAddress("3146 M St NW Washington DC 20007", List(
        AddressPart("address_number_full", "3146"),
        AddressPart("street_name_full", "M St NW"),
        AddressPart("city_name", "Washington"),
        AddressPart("state_name", "DC"),
        AddressPart("zip_code","20007"))),
      ParsedAddress("198 President St, Arkansas City AR 71630", List(
        AddressPart("address_number_full", "198"),
        AddressPart("street_name_full", "President St"),
        AddressPart("city_name", "Arkansas City"),
        AddressPart("state_name", "AR"),
        AddressPart("zip_code","71630"))))

    val source = Source.fromIterator(() => inputParsedList.toIterator)
    val future = source.via(parsedInputAddressFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 3
    result(0).toString() mustBe "1311 30th St NW Washington DC 20007"
    result(0) mustBe SearchableAddress("1311", "30th St NW", "Washington", "20007", "DC")

    result(1).toString() mustBe "3146 M St NW Washington DC 20007"
    result(1) mustBe SearchableAddress("3146", "M St NW", "Washington", "20007", "DC")

    result(2).toString() mustBe "198 President St Arkansas City AR 71630"
    result(2) mustBe SearchableAddress("198", "President St", "Arkansas City", "71630", "AR")
  }

  it must "geocode with State Address Points single field" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source.fromIterator(() => addresses)

    val future = source.via(geocodePointFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    result.size mustBe 1
    result.head.geometry.centroid.roundCoordinates(3) mustBe Point(-77.062, 38.905)
  }

  it must "geocode with all State Address Points fields" in {
    val addresses = List("3146 M St NW Washington DC 20007").toIterator

    val source = Source.fromIterator(() => addresses)
    val futureParsed = source.via(parseFlow).grouped(4).runWith(Sink.head)
    val resultParsed = Await.result(futureParsed, timeout)

    resultParsed.size mustBe 1

    val sourceParsed = Source.fromIterator(() => List(resultParsed.head).toIterator)

    val future = sourceParsed.via(parsedInputAddressFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, timeout)
    println(result)
    result.size mustBe 1

    //FIXME: This does not work.  Probably needs another step in between here
    //result.head.geometry.centroid.roundCoordinates(3) mustBe Point(-77.062, 38.905)
  }

  it must "geocode with TIGER line" in {
    val addresses =
      List("3146 M St NW Washington DC 20007").toIterator

    val source = Source.fromIterator(() => addresses)

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

    val source = Source.fromIterator(() => addresses)

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

    val source = Source.fromIterator(() => addresses)

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
