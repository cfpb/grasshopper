package grasshopper.geometry

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.Checkers
import org.scalacheck.{ Arbitrary, Gen, Properties }
import org.scalacheck.Prop.forAll
import com.vividsolutions.jts.{ geom => jts }

class PointSpec extends PropSpec with PropertyChecks with MustMatchers {

  import GeometryGenerators._

  property("All Points must be valid") {
    forAll { (p: Point) =>
      p.isValid mustBe true
    }
  }

  property("All points must intersect themselves") {
    forAll { (p: Point) =>
      p.intersects(p) mustBe true
    }
  }

  property("A point must not intersect another point") {
    forAll { (p1: Point, p2: Point) =>
      p1.intersects(p2) mustBe false
    }
  }

  property("A point buffer is a polygon") {
    forAll { (p: Point) =>
      p.buffer(1).centroid.roundCoordinates(4) mustBe (p.roundCoordinates(4))
    }
  }

  property("A point must be able to round its coordinates") {
    val p = Point(-161.66663555296856, -85.880232540029)
    p.roundCoordinates(4) mustBe (Point(-161.6666, -85.8802))
  }

  property("A point must serialize to WKT") {
    val p = Point(-77, 39)
    p.wkt mustBe ("POINT (-77 39)")
  }

}
