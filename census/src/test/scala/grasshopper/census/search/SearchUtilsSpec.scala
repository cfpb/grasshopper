package grasshopper.census.search

import grasshopper.census.util.Generators
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class SearchUtilsSpec extends PropSpec with PropertyChecks with MustMatchers with Generators {

  property("Integer should always be numeric") {
    forAll { (a: Int) =>
      SearchUtils.isNumeric(a.toString) mustBe true
    }
  }

  property("Alphanumeric string with letter should never be numeric") {
    forAll(alphanumericGen) { (a: Alphanumeric) =>
      SearchUtils.isNumeric(a.toString) mustBe false
    }
  }
}
