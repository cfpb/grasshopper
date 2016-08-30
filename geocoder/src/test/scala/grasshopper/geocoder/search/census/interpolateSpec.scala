package grasshopper.geocoder.search.census

import org.scalatest.{ MustMatchers, FlatSpec }
import grasshopper.geocoder.search.census.AddressInterpolator._
import grasshopper.model.census.AddressRange
import feature._
import geometry._

class InterpolateSpec extends FlatSpec with MustMatchers {

  "A correct point" must "be found on the right side of a horizontal line" in {
    val values: Map[String, Any] = Map(
      "geometry" -> read("LINESTRING (0 0, 100 0)"),
      "FULLNAME" -> "999 Main Street",
      "RFROMHN" -> 1,
      "RTOHN" -> 101,
      "LFROMHN" -> 0,
      "LTOHN" -> 100,
      "ZIPR" -> "19041",
      "ZIPL" -> "19041",
      "STATE" -> "CA"
    )

    val addressField = Field("address", StringType())
    val geomField = Field("geometry", GeometryType())
    val numberField = Field("number", IntType())
    val schema = Schema(geomField, addressField, numberField)
    val feature = Feature(schema, values)
    val addressNumber = 25
    val addressRange = AddressRange(0, 100)

    val newFeature = interpolate(feature, addressRange, addressNumber)

    newFeature.geometry mustBe ((25, .0001))

  }

}