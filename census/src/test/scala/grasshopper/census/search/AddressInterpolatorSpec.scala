package grasshopper.census.search

import geometry.Point
import grasshopper.census.model.AddressRange
import grasshopper.census.util.TestData._
import org.scalatest.{FlatSpec, MustMatchers}

class AddressInterpolatorSpec extends FlatSpec with MustMatchers {

  "AddressInterpolator" must "extract points at certain distances" in {
    val expected = Point(-77.063, 38.905)
    val ar = AddressRange(100, 300)
    val an = 3150
    val feature = getTigerLine1
    val r = AddressInterpolator.interpolate(feature, ar, an)
    assert(r.geometry.asInstanceOf[Point].roundCoordinates(3) == expected)
  }

  it must "choose the correct address range" in {
    val feature = getTigerLine1
    val an = 3126
    val range = AddressInterpolator.calculateAddressRange(feature, an)
    range.start % 2 mustBe 0
    range.end % 2 mustBe 0
    range.start mustBe 3100
    range.end mustBe 3198
  }

  it must "choose correct address range and interpolate" in {
    val feature = getTigerLine3
    val an = 198
    val range = AddressInterpolator.calculateAddressRange(feature, an)
    range.start % 2 mustBe 0
    range.end % 2 mustBe 0
    range.start mustBe 100
    range.end mustBe 198
  }

  it must "interpolate in other side of the street" in {
    val f = getTigerLine3
    val an = 120
    val r = AddressInterpolator.calculateAddressRange(f, an)
    val expectedRange = AddressRange(100, 198)
    r mustBe expectedRange
    val p = AddressInterpolator.interpolate(f, r, an)
    p.values.get("LFROMHN").get mustBe "100"
    p.values.get("LTOHN").get mustBe "198"

  }
}
