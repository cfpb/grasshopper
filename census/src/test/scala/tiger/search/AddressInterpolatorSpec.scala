package tiger.search

import feature.Feature
import geometry.{ Point, Line }
import org.scalatest.{ MustMatchers, FlatSpec }
import tiger.model.AddressRange
import spray.json._
import io.geojson.FeatureJsonProtocol._

class AddressInterpolatorSpec extends FlatSpec with MustMatchers {

  import AddressInterpolator._

  val p1 = Point(-77, 39)
  val p2 = Point(-76, 40)
  val p3 = Point(-75, 38)
  val p4 = Point(-77, 39)
  val p5 = Point(-78, 39.1)
  val line = Line(Array(p1, p2, p3))

  val fjson = """{ "type": "Feature", "properties": { "TLID": 76225010, "TFIDL": 210419286, "TFIDR": 210415483, "ARIDL": "400331412920", "ARIDR": "400331428599", "LINEARID": "110431686609", "FULLNAME": "M St NW", "LFROMHN": "3100", "LTOHN": "3198", "RFROMHN": "3101", "RTOHN": "3199", "ZIPL": "20007", "ZIPR": "20007", "EDGE_MTFCC": "S1400", "ROAD_MTFCC": "S1400", "PARITYL": "E", "PARITYR": "O", "PLUS4L": null, "PLUS4R": null, "LFROMTYP": null, "LTOTYP": null, "RFROMTYP": null, "RTOTYP": null, "OFFSETL": "N", "OFFSETR": "N", "STATE": "DC" }, "geometry": { "type": "LineString", "coordinates": [ [ -77.061184, 38.90519 ], [ -77.061468, 38.90519 ], [ -77.061704, 38.905185 ], [ -77.061965, 38.905186 ], [ -77.062709, 38.905177 ], [ -77.062811, 38.905177 ] ] } }"""

  "AddressInterpolator" should "extract points at certain distances" in {
    val expected = Point(-77.06179372923049, 38.905285344524145)
    val ar = AddressRange(100, 300)
    val an = 225
    val f = fjson.parseJson.convertTo[Feature]
    val r = AddressInterpolator.interpolate(f, ar, an)
    assert(r.geometry == expected)
  }

  it should "choose the correct address range" in {
    val feature = fjson.parseJson.convertTo[Feature]
    val an = 3126
    val range = AddressInterpolator.calculateAddressRange(feature, an)
    range.start % 2 mustBe (0)
    range.end % 2 mustBe (0)
    range.start mustBe (3100)
    range.end mustBe (3198)
  }
}
