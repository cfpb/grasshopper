package grasshopper.addresspoints.search

import org.scalatest.{ MustMatchers, FlatSpec }

class SearchUtilsSpec extends FlatSpec with MustMatchers {

  "SearchUtils" should "calculate Levenshtein distance between two strings" in {
    SearchUtils.levenshtein("Hello", "Hallo") mustBe 1
    SearchUtils.levenshtein("Grasshopper is a fine geocoder", "Grasshopper is a nice geocoder") mustBe 2
  }

  it should "report percentage match between 2 strings" in {
    SearchUtils.percentMatch("Hello", "Hallo") mustBe 0.8
    SearchUtils.percentMatch("Grasshopper is a good geocoder", "Grasshopper is a nice Ge0coder") mustBe 0.8
  }

}
