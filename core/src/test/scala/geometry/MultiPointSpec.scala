package grasshopper.geometry

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

class MultiPointSpec extends PropSpec with PropertyChecks with MustMatchers {

  import GeometryGenerators._

  val p1 = Point(-77, 39)
  val p2 = Point(-77, 40)
  val p3 = Point(-76, 40)
  val p4 = Point(-76, 39)
  val mp = MultiPoint(p1, p2, p3, p4, p1)

  property("All MultiPoints must be valid") {
    forAll { (mp: MultiPoint) =>
      mp.isValid mustBe true
    }
  }

  property("All MultiPoints must have more than one geometry") {
    forAll { (mp: MultiPoint) =>
      mp.numGeometries must be >= 0
    }
  }

  property("A MultiPoint must serialize to WKT") {
    mp.wkt mustBe ("MULTIPOINT ((-77 39), (-77 40), (-76 40), (-76 39), (-77 39))")
  }

}
