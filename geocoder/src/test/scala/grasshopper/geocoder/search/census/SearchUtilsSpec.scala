package grasshopper.geocoder.search.census

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import grasshopper.geocoder.search.census.SearchUtils._

class SearchUtilsSpec extends PropSpec with MustMatchers with PropertyChecks with NumericGenerators {

  property("digits are parsed into Ints") {
    forAll(digits) { n =>
      toInt(n.toString) mustBe Some(n)
    }
  }

  property("decimals are parsed into Ints") {
    forAll(digits, positive) { (x, y) =>
      val numString = x.toString + "." + y.toString
      toInt(numString) mustBe Some(x)
    }
  }

  property("hyphens are parsed into Ints") {
    forAll(digits, positive) { (x, y) =>
      val numString = x.toString + "-" + y.toString
      toInt(numString) mustBe Some(x)
    }
  }
}
