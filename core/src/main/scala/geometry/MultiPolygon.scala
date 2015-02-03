package grasshopper.geometry

import scala.language.implicitConversions
import com.vividsolutions.jts.{ geom => jts }

object MultiPolygon {

  private val geomFactory = new jts.GeometryFactory

  def apply(polygons: Polygon*): MultiPolygon = {
    apply(polygons.toArray)
  }

  def apply(polygons: Array[Polygon]): MultiPolygon = {
    val jtsPolygons = polygons.map(p => p.jtsGeometry)
    MultiPolygon(new jts.MultiPolygon(jtsPolygons, geomFactory))
  }

  def apply(polygons: Array[Polygon], srid: Int): MultiPolygon = {
    val jtsPolygons = polygons.map(p => p.jtsGeometry)
    val gf = new jts.GeometryFactory(null, srid)
    MultiPolygon(new jts.MultiPolygon(jtsPolygons, gf))
  }

}

case class MultiPolygon(jtsGeometry: jts.MultiPolygon) extends GeometryCollection
