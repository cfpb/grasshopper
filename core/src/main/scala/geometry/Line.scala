package grasshopper.geometry

import scala.language.implicitConversions
import com.vividsolutions.jts.{ geom => jts }
import jts.GeometryFactory
import jts.LineString
import jts.impl.CoordinateArraySequence
import jts.PrecisionModel

object Line {

  private val geomFactory = new jts.GeometryFactory

  def apply(points: Point*): Line = {
    apply(points.toList)
  }

  def apply(points: Traversable[Point]): Line = {
    Line(geomFactory.
      createLineString(Util.points2JTSCoordinates(points).toArray))
  }

  def apply(points: Traversable[Point], srid: Int): Line = {
    val gf = new jts.GeometryFactory(new PrecisionModel, srid)
    Line(gf.
      createLineString(Util.points2JTSCoordinates(points).toArray))
  }

  implicit def jtsToLine(jtsGeom: jts.LineString): Line = {
    apply(jtsGeom)
  }

}

case class Line(jtsGeometry: jts.LineString) extends Geometry {

  def length: Double = {
    jtsGeometry.getLength
  }

  def numPoints: Int = {
    jtsGeometry.getNumPoints
  }

  def startPoint: Point = {
    Point(jtsGeometry.getStartPoint)
  }

  def endPoint: Point = {
    Point(jtsGeometry.getEndPoint)
  }

  def isClosed: Boolean = {
    jtsGeometry.isClosed
  }

  def isRing: Boolean = {
    jtsGeometry.isRing
  }

  def reverse: Line = {
    Line(jtsGeometry.reverse.asInstanceOf[LineString])
  }

  def pointAt(n: Int): Point = {
    Point(jtsGeometry.getPointN(n))
  }

  def pointAtDist(d: Double): Point = {
    val coord = new com.vividsolutions.jts.linearref.LengthIndexedLine(jtsGeometry).extractPoint(d)
    Point(coord.x, coord.y)
  }

  def pointAtDistWithOffset(d: Double, offset: Double): Point = {
    val coord = new com.vividsolutions.jts.linearref.LengthIndexedLine(jtsGeometry).extractPoint(d, offset)
    Point(coord.x, coord.y)
  }

  def isCoordinate(point: Point): Boolean = {
    jtsGeometry.isCoordinate(point.jtsGeometry.getCoordinate)
  }

}
