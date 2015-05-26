package grasshopper.census.search

import feature.Feature
import geometry.{ Point, Line }
import org.scalatest.{ MustMatchers, FlatSpec }
import grasshopper.census.model.AddressRange
import spray.json._
import io.geojson.FeatureJsonProtocol._

class AddressInterpolatorSpec extends FlatSpec with MustMatchers {

  val p1 = Point(-77, 39)
  val p2 = Point(-76, 40)
  val p3 = Point(-75, 38)
  val p4 = Point(-77, 39)
  val p5 = Point(-78, 39.1)
  val line = Line(Array(p1, p2, p3))

  val fjson = """{ "type": "Feature", "properties": { "TLID": 76225010, "TFIDL": 210419286, "TFIDR": 210415483, "ARIDL": "400331412920", "ARIDR": "400331428599", "LINEARID": "110431686609", "FULLNAME": "M St NW", "LFROMHN": "3100", "LTOHN": "3198", "RFROMHN": "3101", "RTOHN": "3199", "ZIPL": "20007", "ZIPR": "20007", "EDGE_MTFCC": "S1400", "ROAD_MTFCC": "S1400", "PARITYL": "E", "PARITYR": "O", "PLUS4L": null, "PLUS4R": null, "LFROMTYP": null, "LTOTYP": null, "RFROMTYP": null, "RTOTYP": null, "OFFSETL": "N", "OFFSETR": "N", "STATE": "DC" }, "geometry": { "type": "LineString", "coordinates": [ [ -77.061184, 38.90519 ], [ -77.061468, 38.90519 ], [ -77.061704, 38.905185 ], [ -77.061965, 38.905186 ], [ -77.062709, 38.905177 ], [ -77.062811, 38.905177 ] ] } }"""

  "AddressInterpolator" must "extract points at certain distances" in {
    val expected = Point(-77.062, 38.905)
    val ar = AddressRange(100, 300)
    val an = 225
    val f = fjson.parseJson.convertTo[Feature]
    val r = AddressInterpolator.interpolate(f, ar, an)
    assert(r.geometry.asInstanceOf[Point].roundCoordinates(3) == expected)
  }

  it must "choose the correct address range" in {
    val feature = fjson.parseJson.convertTo[Feature]
    val an = 3126
    val range = AddressInterpolator.calculateAddressRange(feature, an)
    range.start % 2 mustBe 0
    range.end % 2 mustBe 0
    range.start mustBe 3100
    range.end mustBe 3198
  }

  it must "choose correct address range and interpolate" in {
    val pjson = """{"type":"Feature","properties":{"TLID":25901822,"TFIDL":201632875,"TFIDR":201632723,"ARIDL":"","ARIDR":"40086023938","LINEARID":"11092269911","FULLNAME":"President St","LFROMHN":"","LTOHN":"","RFROMHN":"100","RTOHN":"498","ZIPL":"","ZIPR":"71630","EDGE_MTFCC":"S1400","ROAD_MTFCC":"S1400","PARITYL":"","PARITYR":"E","PLUS4L":"","PLUS4R":"","LFROMTYP":"","LTOTYP":"","RFROMTYP":"","RTOTYP":"I","OFFSETL":"N","OFFSETR":"N","load_date":1431448963039,"STATE":"AR"},"geometry":{"type":"LineString","coordinates":[[-91.19945,33.60725],[-91.19951,33.60734],[-91.20001,33.60803],[-91.20041,33.60853]]}}"""
    val feature = pjson.parseJson.convertTo[Feature]
    val an = 198
    val range = AddressInterpolator.calculateAddressRange(feature, an)
    range.start % 2 mustBe 0
    range.end % 2 mustBe 0
    range.start mustBe 100
    range.end mustBe 498
  }
}
