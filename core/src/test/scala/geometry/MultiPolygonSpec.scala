package grasshopper.geometry

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

class MultiPolygonSpec extends PropSpec with PropertyChecks with MustMatchers {

  import GeometryGenerators._

  val p1 = Point(-77, 39)
  val p2 = Point(-76, 40)
  val p3 = Point(-75, 38)
  val p4 = Point(-77, 39)
  val p5 = Point(-78, 32)
  val p6 = Point(-77, 32)
  val p7 = Point(-76, 35)
  val p8 = Point(-75, 31)
  val polygon1 = Polygon(p1, p2, p3, p4)
  val polygon2 = Polygon(p6, p7, p8, p6)
  val mp = MultiPolygon(polygon1, polygon2)

  property("A MultiPolygon must be valid") {
    mp.isValid mustBe true
  }

  property("A MultiPolygon has a number of geometries") {
    mp.numGeometries must be >= 0
  }

}
