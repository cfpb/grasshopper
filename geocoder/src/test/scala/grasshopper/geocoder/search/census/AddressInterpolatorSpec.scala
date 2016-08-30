package grasshopper.geocoder.search.census

import org.scalatest.{ PropSpec, MustMatchers, PrivateMethodTester }
import org.scalatest.prop.PropertyChecks
import grasshopper.geocoder.search.census.AddressInterpolator._
import grasshopper.model.census.AddressRange
import feature._

class AddressInterpolatorSpec extends PropSpec with MustMatchers with PropertyChecks with PrivateMethodTester with NumericGenerators {

  property("calculateDistance must always be positive") {
    forAll(positive, positive) { (start, stop) =>
      val range = new AddressRange(start, stop)
      val calculateDistance = PrivateMethod[Double]('calculateDistance)
      AddressInterpolator invokePrivate calculateDistance(range) mustBe >=(0.0)
    }
  }

  val testField = new Field("test", StringType())
  val testSchema = new Schema(Iterable(testField, testField))

  property("for rangeIsEven left even is even") {
    forAll(even, even) { (x, y) =>
      val range = Map("LFROMHN" -> x, "LTOHN" -> y)
      val evenFeature = new Feature(1, testSchema, range)
      val rangeIsEven = PrivateMethod[Boolean]('rangeIsEven)
      AddressInterpolator invokePrivate rangeIsEven(evenFeature, true) mustBe true
    }
  }

  property("for rangeIsEven left odd is odd") {
    forAll(odd, odd) { (x, y) =>
      whenever(x != 0 && y != 0) {
        val range = Map("LFROMHN" -> x, "LTOHN" -> y)
        val oddFeature = new Feature(1, testSchema, range)
        val rangeIsEven = PrivateMethod[Boolean]('rangeIsEven)
        AddressInterpolator invokePrivate rangeIsEven(oddFeature, true) mustBe false
      }
    }
  }

  property("for rangeIsEven right even is even") {
    forAll(even, even) { (x, y) =>
      val range = Map("RFROMHN" -> x, "RTOHN" -> y)
      val evenFeature = new Feature(1, testSchema, range)
      val rangeIsEven = PrivateMethod[Boolean]('rangeIsEven)
      AddressInterpolator invokePrivate rangeIsEven(evenFeature, false) mustBe true
    }
  }

  property("for rangeIsEven right odd is odd") {
    forAll(odd, odd) { (x, y) =>
      whenever(x != 0 && y != 0) {
        val range = Map("RFROMHN" -> x, "RTOHN" -> y)
        val oddFeature = new Feature(1, testSchema, range)
        val rangeIsEven = PrivateMethod[Boolean]('rangeIsEven)
        AddressInterpolator invokePrivate rangeIsEven(oddFeature, false) mustBe false
      }
    }
  }

  property("for calculateAddressRange left even, right odd") {
    forAll(odd, odd, even, even, digits) { (r1, r2, l1, l2, a) =>
      whenever(r1 != 0 && r2 != 0) {
        val range = Map("RFROMHN" -> r1, "RTOHN" -> r2, "LFROMHN" -> l1, "LTOHN" -> l2)
        val feature = new Feature(1, testSchema, range)
        if (a % 2 == 0) calculateAddressRange(feature, a) mustBe (AddressRange(l1, l2))
        else calculateAddressRange(feature, a) mustBe (AddressRange(r1, r2))
      }
    }
  }

  property("for calculateAddressRange left odd, right even") {
    forAll(even, even, odd, odd, digits) { (r1, r2, l1, l2, a) =>
      whenever(l1 != 0 && l2 != 0) {
        val range = Map("RFROMHN" -> r1, "RTOHN" -> r2, "LFROMHN" -> l1, "LTOHN" -> l2)
        val feature = new Feature(1, testSchema, range)
        if (a % 2 == 0) calculateAddressRange(feature, a) mustBe (AddressRange(r1, r2))
        else calculateAddressRange(feature, a) mustBe (AddressRange(l1, l2))
      }
    }
  }

}