package grasshopper.geometry

import scala.language.implicitConversions
import com.vividsolutions.jts.{ geom => jts }

object MultiPoint {

  private val geomFactory = new jts.GeometryFactory

  def apply(points: Point*): MultiPoint = {
    apply(points.toArray)
  }

  def apply(points: Array[Point]): MultiPoint = {
    val jtsPoints = points.map(p => p.jtsGeometry)
    MultiPoint(new jts.MultiPoint(jtsPoints, geomFactory))
  }

  def apply(points: Traversable[Point]): MultiPoint = {
    val jtsPoints = points.map(p => p.jtsGeometry).toArray
    MultiPoint(new jts.MultiPoint(jtsPoints, geomFactory))
  }
  def apply(points: Array[Point], srid: Int): MultiPoint = {
    val jtsPoints = points.map(p => p.jtsGeometry)
    val gf = new jts.GeometryFactory(null, srid)
    MultiPoint(new jts.MultiPoint(jtsPoints, gf))
  }

  def apply(points: Traversable[Point], srid: Int): MultiPoint = {
    val jtsPoints = points.map(p => p.jtsGeometry).toArray
    val gf = new jts.GeometryFactory(null, srid)
    MultiPoint(new jts.MultiPoint(jtsPoints, gf))
  }
}

case class MultiPoint(jtsGeometry: jts.MultiPoint) extends GeometryCollection
