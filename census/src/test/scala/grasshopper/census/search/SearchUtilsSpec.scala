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

  property("Alphanumeric string with letter should convert to Int") {
    forAll(alphanumericGen) { (a: Alphanumeric) =>
      val s = SearchUtils.toInt(a.toString).getOrElse(0).toString
      SearchUtils.isNumeric(s) mustBe true
    }
  }

  property("String with hyphen should never be numeric") {
    forAll(hyphenStrGen) { (h: HyphenStr) =>
      SearchUtils.isNumeric(h.toString) mustBe false
      val s = SearchUtils.toInt(h.toString).getOrElse(0).toString
      SearchUtils.isNumeric(s) mustBe true
    }
  }

  property("Hyphenated string should convert to Int") {
    forAll(hyphenStrGen) { (a: HyphenStr) =>
      val s = SearchUtils.toInt(a.toString).getOrElse(0).toString
      SearchUtils.isNumeric(s) mustBe true
    }
  }

}
