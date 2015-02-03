package grasshopper.geometry

import scala.math.BigDecimal
import scala.language.implicitConversions
import com.vividsolutions.jts.{ geom => jts }
import jts.Coordinate
import jts.GeometryFactory
import jts.PrecisionModel

object Point {

  private val geomFactory = new jts.GeometryFactory

  def apply(x: Double, y: Double): Point = {
    Point(geomFactory.createPoint(new jts.Coordinate(x, y, 0)))
  }

  def apply(x: Double, y: Double, srid: Int): Point = {
    val gf = new jts.GeometryFactory(new PrecisionModel, srid)
    Point(gf.createPoint(new jts.Coordinate(x, y, 0)))
  }

  def apply(x: Double, y: Double, z: Double): Point = {
    Point(geomFactory.createPoint(new jts.Coordinate(x, y, z)))
  }

  def apply(x: Double, y: Double, z: Double, srid: Int): Point = {
    val gf = new jts.GeometryFactory(new PrecisionModel, srid)
    Point(gf.createPoint(new jts.Coordinate(x, y, z)))
  }

  implicit def jtsToPoint(jtsGeom: jts.Point): Point = {
    apply(jtsGeom)
  }

}

case class Point(jtsGeometry: jts.Point) extends Geometry {

  private def roundAt(p: Int, n: Double): Double = {
    val s = math.pow(10, p)
    math.round(n * s) / s
  }

  def x: Double = jtsGeometry.getX

  def y: Double = jtsGeometry.getY

  def z: Double = jtsGeometry.getCoordinate.z

  def roundCoordinates(s: Int): Point = {
    val xr = roundAt(s, x)
    val yr = roundAt(s, y)
    val zr = roundAt(s, z)
    Point(xr, yr, zr)
  }

}
