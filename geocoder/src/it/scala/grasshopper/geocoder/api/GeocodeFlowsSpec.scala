package grasshopper.geocoder.api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import geometry.Point
import grasshopper.client.census.model.ParsedInputAddress
import grasshopper.client.parser.model.{AddressPart, ParsedAddress}
import grasshopper.geocoder.model.ParsedOutputBatchAddress
import org.scalatest.{FlatSpec, MustMatchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class GeocodeFlowsSpec extends FlatSpec with MustMatchers {

  implicit val system = ActorSystem("sys")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  "GeocodeFlows" must "parse a list of addresses" in {
    val addresses =
      List(
        "1311 30th St NW Washington DC 20007",
        "3146 M St NW Washington DC 20007",
        "198 President St Arkansas City AR 71630",
        "1 Main St City ST 00001"
      ).toIterator

    val source = Source(() => addresses)
    val future = source.via(GeocodeFlows.parseFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, 2.seconds)
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

  it must "transform parsed addresses into batch parsed addresses" in {
    val inputParsedList = List(
      ParsedAddress("1311 30th St NW Washington DC 20007",
        AddressPart("1311","Washington","DC","30th St NW","20007")),
      ParsedAddress("3146 M St NW Washington DC 20007",
        AddressPart("3146","Washington","DC","M St NW","20007")),
      ParsedAddress("198 President St Arkansas City AR 71630",
        AddressPart("198","Arkansas City","AR","President St","71630")),
      ParsedAddress("1 Main St City ST 00001",AddressPart("1","St City","ST","Main","00001")))

    val source = Source(() => inputParsedList.toIterator)
    val future = source.via(GeocodeFlows.parsedInputAddressFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, 2.seconds)
    result.size mustBe 4
    result.head.input mustBe "1311 30th St NW Washington DC 20007"
    result.head.parsed mustBe ParsedInputAddress(1311, "30th St NW", "20007", "DC")

    result.tail.head.input mustBe "3146 M St NW Washington DC 20007"
    result.tail.head.parsed mustBe ParsedInputAddress(3146, "M St NW", "20007", "DC")
  }

  it must "geocode batch parsed addresses into TIGER line results" in {
    val parsedBatchList =
      List(
        ParsedOutputBatchAddress("1311 30th St NW Washington DC 20007",
          ParsedInputAddress(1311, "30th St NW", "20007", "DC")),
        ParsedOutputBatchAddress("3146 M St NW Washington DC 20007",
          ParsedInputAddress(3146, "M St NW","20007", "DC")),
        ParsedOutputBatchAddress("198 President St Arkansas City AR 71630",
          ParsedInputAddress(198, "President St", "71630", "AR")),
        ParsedOutputBatchAddress("1 Main St City ST 00001",
          ParsedInputAddress(1, "Main ST", "1","ST")))

    val source = Source(() => parsedBatchList.toIterator)
    val future = source.via(GeocodeFlows.censusFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, 2.seconds)
    result.size mustBe 4
    result.head.input mustBe "1311 30th St NW Washington DC 20007"
    val point = Point(result.head.longitude, result.head.latitude).roundCoordinates(3)
    point mustBe Point(-77.059, 38.907)
  }

  it must "geocode addresses into points" in {
    val addresses =
      List(
        "1311 30th St NW Washington DC 20007",
        "3146 M St NW Washington DC 20007",
        "198 President St Arkansas City AR 71630",
        "1 Main St City ST 00001"
      ).toIterator

    val source = Source(() => addresses)
    val future = source.via(GeocodeFlows.addressPointsFlow).grouped(4).runWith(Sink.head)
    val result = Await.result(future, 2.seconds)
    result.size mustBe 4
    result.tail.tail.head.input mustBe "198 President St Arkansas City AR 71630"
    result.tail.tail.head.latitude mustBe 33.60824683723317
    result.tail.tail.head.longitude mustBe -91.19986550300061

  }
}
