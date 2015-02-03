package grasshopper.geometry

import org.scalacheck.{ Arbitrary, Prop, Gen }
import org.scalacheck.Prop.forAll
import com.vividsolutions.jts.{ geom => jts }

object GeometryGenerators {

  def round(d: Double): Double = {
    val x = d * 100
    val y = Math.round(x)
    y * 100
  }

  val points: Gen[Point] = {
    for {
      x <- Gen.choose(-180.0, 180.0)
      y <- Gen.choose(-90.0, 90.0)
      z <- Arbitrary.arbitrary[Double]
    } yield Point(x, y, z)
  }

  val pointList: Gen[List[Point]] = {
    for {
      pts <- Gen.listOfN[Point](50, points)
    } yield pts
  }

  val lines: Gen[Line] = {
    for {
      pts <- Gen.listOfN[Point](10, points)
    } yield Line(pts)
  }

  val closedLines: Gen[Line] = {
    for {
      p <- points
      l <- lines
    } yield Line(p :: (p :: l.points.toList).reverse)
  }

  val polygons: Gen[Polygon] = {
    for {
      p <- points
      l <- closedLines
    } yield Polygon(p :: (p :: l.points.toList).reverse)
  }

  val multipoints: Gen[MultiPoint] = {
    for {
      pts <- Gen.listOf[Point](points)
    } yield MultiPoint(pts)
  }

  val multilines: Gen[MultiLine] = {
    for {
      lines <- Gen.listOf[Line](lines)
    } yield MultiLine(lines)
  }

  //val multipolygons: Gen[MultiPolygon] = {
  //  for {
  //    polys <- Gen.listOf[Polygon](polygons)
  //  } yield MultiPolygon(polys)
  //}

  implicit val arbPoint: Arbitrary[Point] = Arbitrary(points)
  implicit val arbLine: Arbitrary[Line] = Arbitrary(lines)
  implicit val arbPolygon: Arbitrary[Polygon] = Arbitrary(polygons)
  implicit val arbMultiPoint: Arbitrary[MultiPoint] = Arbitrary(multipoints)
  implicit val arbMultiLine: Arbitrary[MultiLine] = Arbitrary(multilines)
  //implicit val arbMultiPolygon: Arbitrary[MultiPolygon] = Arbitrary(multipolygons)
}

