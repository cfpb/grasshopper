package grasshopper.geojson

import grasshopper.geometry._
import grasshopper.feature._
import spray.json._
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.Checkers

class FeatureJsonProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  import grasshopper.geojson.FeatureJsonProtocol._

  val p1 = Point(-77, 39)
  val values = Map("Description" -> "First Point", "id" -> "0000-0000", "Value" -> 1.5)
  val f1 = Feature(p1, values)
  val pJson = """{"type":"Feature","geometry":{"type":"Point","coordinates":[-77.0,39.0,0.0]},"properties":{"Description":"First Point","id":"0000-0000","Value":1.5}}"""

  property("Feature must write to GeoJSON") {
    f1.toJson.toString mustBe (pJson)
  }

  property("GeoJSON must read into Feature") {
    val f = pJson.parseJson.convertTo[Feature]
    f.geometry mustBe (f1.geometry)
    f.values mustBe (f1.values)
  }

}
