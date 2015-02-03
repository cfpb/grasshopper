package grasshopper.geometry

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

class PolygonSpec extends PropSpec with PropertyChecks with MustMatchers {

  import GeometryGenerators._

  val p1 = Point(-77, 39)
  val p2 = Point(-76, 40)
  val p3 = Point(-75, 38)
  val p4 = Point(-77, 39)
  val p5 = Point(-78, 39.1)
  val p6 = Point(-75.7, 39.2)
  val p7 = Point(-76.5, 39)
  val p8 = Point(-76, 38.5)
  val polygon = Polygon(p1, p2, p3, p4)
  val polygonFromLine = Polygon(Line(Array(p1, p2, p3, p4)))
  val hole = Polygon(p6, p7, p8, p6)
  val boundary = Line(p1, p2, p3, p4)
  val ring = Line(p6, p7, p8, p6)
  val polyWithHole = Polygon(boundary, ring)

  property("A Polygon must be valid") {
    polygon.isValid mustBe true
    polygonFromLine.isValid mustBe true
    hole.isValid mustBe true
  }

  property("A Polygon must have a perimeter") {
    polygon.perimeter must be > 0.0
    polygonFromLine.perimeter must be > 0.0
  }

  property("A Polygon must have an area") {
    polygon.area must be > 0.0
    polygonFromLine.area must be > 0.0
  }

  property("A Polygon must be able to contain holes") {
    polyWithHole.isValid mustBe true
  }

  property("A Polygon must serialize to WKT") {
    polygon.wkt mustBe ("POLYGON ((-77 39, -76 40, -75 38, -77 39))")
    polygonFromLine.wkt mustBe ("POLYGON ((-77 39, -76 40, -75 38, -77 39))")
  }

  property("A Polygon's boundary is a line") {
    polygon.boundary mustBe (Line(p1, p2, p3, p1))
  }

  property("A Polygon's interior ring is a list of lines") {
    polyWithHole.holes.size mustBe (1)
  }
}

