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

  "Census Geocode" must "find the correct line segment from an address" in {
    val addressInput = ParsedAddressInput(
      3146,
      "M St NW",
      20007,
      "DC"
    )

    val expectedPoint = Point(-77.06204609363698, 38.90508501171226)
    val expectedValues = Map("geometry" -> expectedPoint, "address" -> "3146 M St NW")
    val expectedSchema = Schema(
      List(
        Field("geometry", GeometryType()),
        Field("address", StringType())
      )
    )

    val expectedFeature = Feature(expectedSchema, expectedValues)
    val features = geocodeLine(client, "census", "addrfeat", addressInput, 1) getOrElse emptyFeatures
    assert(features(0).geometry == expectedFeature.geometry)
    assert(features(0).values.get("address").toString == expectedFeature.values.get("address").toString)
  }

}
