package grasshopper.geometry

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

class MultiLineSpec extends PropSpec with PropertyChecks with MustMatchers {

  import GeometryGenerators._

  val p1 = Point(-77, 39)
  val p2 = Point(-76, 40)
  val p3 = Point(-75, 38)
  val p4 = Point(-77, 39)
  val p5 = Point(-78, 39.1)
  val line = Line(Array(p1, p2, p3))
  val closedLine = Line(Array(p1, p2, p3, p4))
  val l = MultiLine(line, closedLine)

  property("All MultiLines must be valid") {
    forAll { (ml: MultiLine) =>
      ml.isValid mustBe true
    }
  }

  property("All MultiLines have a reverse") {
    forAll { (ml: MultiLine) =>
      ml.reverse.reverse mustBe (ml)
    }
  }

  property("A MultiLine has a number of geometries") {
    l.numGeometries mustBe (2)
  }

  property("A MultiLine must serialize to WKT") {
    l.wkt mustBe ("MULTILINESTRING ((-77 39, -76 40, -75 38), (-77 39, -76 40, -75 38, -77 39))")
  }

}
