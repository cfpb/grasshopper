package grasshopper.census.search

import org.scalatest.{ FlatSpec, MustMatchers, BeforeAndAfterAll }
import geometry._
import feature._
import grasshopper.elasticsearch.ElasticsearchServer
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import grasshopper.census.model.ParsedInputAddress
import grasshopper.census.util.TestData._

class CensusGeocodeSpec extends FlatSpec with MustMatchers with BeforeAndAfterAll with CensusGeocode {

  val server = new ElasticsearchServer
  val client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("census")
    server.loadFeature("census", "addrfeat", getTigerLine1)
    server.loadFeature("census", "addrfeat", getTigerLine2)
    server.loadFeature("census", "addrfeat", getTigerLine3)
    server.loadFeature("census", "addrfeat", getTigerLine4)
    server.loadFeature("census", "addrfeat", getTigerLine5)
    client.admin().indices().refresh(new RefreshRequest("census")).actionGet()
  }

  override def afterAll = {
    server.stop()
  }

  "Census Geocode" must "find address" in {
    val addressInput = ParsedInputAddress(
      "3146",
      "M St NW",
      "20007",
      "DC"
    )

    val features = geocodeLine(client, "census", "addrfeat", addressInput, 1) getOrElse emptyFeatures
    log.info(features.toString)
    features(0).get("FULLNAME").getOrElse("") mustBe "M St NW"
  }

  "Census Geocode" must "interpolate an address location from a line segment" in {
    val addressInput = ParsedInputAddress(
      "3146",
      "M St NW",
      "20007",
      "DC"
    )

    val expectedPoint = Point(-77.062, 38.905)
    val expectedValues = Map(
      "geometry" -> expectedPoint,
      "FULLNAME" -> "M St NW",
      "ZIPL" -> "20007",
      "ZIPR" -> "20007",
      "LFROMHN" -> "3100",
      "LTOHN" -> "3198",
      "RFROMHN" -> "3101",
      "RTOHN" -> "3199",
      "STATE" -> "DC"

    )
    val expectedSchema = Schema(
      List(
        Field("geometry", GeometryType()),
        Field("FULLNAME", StringType()),
        Field("ZIPL", StringType()),
        Field("ZIPR", StringType()),
        Field("LFROMHN", StringType()),
        Field("LTOHN", StringType()),
        Field("RFROMHN", StringType()),
        Field("RTOHN", StringType()),
        Field("STATE", StringType())
      )
    )

    val expectedFeature = Feature(expectedSchema, expectedValues)
    val features = geocodeLine(client, "census", "addrfeat", addressInput, 1) getOrElse emptyFeatures
    features(0).geometry.asInstanceOf[Point].roundCoordinates(3) mustBe expectedFeature.geometry
    features(0).values.getOrElse("FULLNAME", "") mustBe expectedFeature.values.getOrElse("FULLNAME", "")
    features(0).values.getOrElse("ZIPL", "") mustBe expectedFeature.values.getOrElse("ZIPL", "")
    features(0).values.getOrElse("ZIPR", "") mustBe expectedFeature.values.getOrElse("ZIPR", "")
    features(0).values.getOrElse("LFROMHN", "") mustBe expectedFeature.values.getOrElse("LFROMHN", "")
    features(0).values.getOrElse("LTOHN", "") mustBe expectedFeature.values.getOrElse("LTOHN", "")
    features(0).values.getOrElse("RFROMHN", "") mustBe expectedFeature.values.getOrElse("RFROMHN", "")
    features(0).values.getOrElse("RTOHN", "") mustBe expectedFeature.values.getOrElse("RTOHN", "")
    features(0).values.getOrElse("STATE", "") mustBe expectedFeature.values.getOrElse("STATE", "")

  }

  "Census Geocode" must "interpolate address locations from line segments with non-numeric address range" in {
    val addressInput = ParsedInputAddress(
      "G41",
      "Motel Rd",
      "80915",
      "CO"
    )

    val addressInput2 = ParsedInputAddress(
      "125-2",
      "S Reynolds St",
      "22304",
      "VA"
    )

    val expectedPoint = Point(-104.711, 38.837)
    val expectedPoint2 = Point(-77.128, 38.811)

    val expectedValues = Map(
      "geometry" -> expectedPoint,
      "FULLNAME" -> "Motel Rd",
      "ZIPL" -> "",
      "ZIPR" -> "80915",
      "LFROMHN" -> "",
      "LTOHN" -> "",
      "RFROMHN" -> "G1",
      "RTOHN" -> "G99",
      "STATE" -> "CO"
    )
    val expectedValues2 = Map(
      "geometry" -> expectedPoint2,
      "FULLNAME" -> "S Reynolds St",
      "ZIPL" -> "",
      "ZIPR" -> "22304",
      "LFROMHN" -> "",
      "LTOHN" -> "",
      "RFROMHN" -> "125-0",
      "RTOHN" -> "125-6",
      "STATE" -> "VA"
    )

    val expectedSchema = Schema(
      List(
        Field("geometry", GeometryType()),
        Field("FULLNAME", StringType()),
        Field("ZIPL", StringType()),
        Field("ZIPR", StringType()),
        Field("LFROMHN", StringType()),
        Field("LTOHN", StringType()),
        Field("RFROMHN", StringType()),
        Field("RTOHN", StringType()),
        Field("STATE", StringType())
      )
    )

    val expectedFeature = Feature(expectedSchema, expectedValues)
    val expectedFeature2 = Feature(expectedSchema, expectedValues2)
    val features = geocodeLine(client, "census", "addrfeat", addressInput, 1) getOrElse emptyFeatures
    val features2 = geocodeLine(client, "census", "addrfeat", addressInput2, 1) getOrElse emptyFeatures
    features(0).geometry.asInstanceOf[Point].roundCoordinates(3) mustBe expectedFeature.geometry
    features2(0).geometry.asInstanceOf[Point].roundCoordinates(3) mustBe expectedFeature2.geometry
  }

}
