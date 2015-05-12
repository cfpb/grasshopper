package tiger.search

import org.scalatest.{ FlatSpec, MustMatchers, BeforeAndAfterAll }
import geometry._
import feature._
import grasshopper.elasticsearch.ElasticsearchServer
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import tiger.model.ParsedAddressInput
import tiger.util.TestData._

class CensusGeocodeSpec extends FlatSpec with MustMatchers with BeforeAndAfterAll with CensusGeocode {

  val server = new ElasticsearchServer
  val client = server.client

  override def beforeAll = {
    server.start()
    server.createAndWaitForIndex("census")
    server.loadFeature("census", "addrfeat", getTigerLine1)
    client.admin().indices().refresh(new RefreshRequest("census")).actionGet()
  }

  override def afterAll = {
    server.stop()
  }

  "Census Geocode" must "interpolate an address location from a line segment" in {
    val addressInput = ParsedAddressInput(
      3146,
      "M St NW",
      20007,
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

}
