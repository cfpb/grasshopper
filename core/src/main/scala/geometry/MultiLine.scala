package grasshopper.geometry

import scala.language.implicitConversions
import com.vividsolutions.jts.{ geom => jts }

object MultiLine {

  private val geomFactory = new jts.GeometryFactory

  def apply(lines: Line*): MultiLine = {
    apply(lines.toArray)
  }

  def apply(lines: List[Line]): MultiLine = {
    apply(lines.toArray)
  }

  def apply(lines: Array[Line]): MultiLine = {
    val jtsLines = lines.map(l => l.jtsGeometry)
    MultiLine(new jts.MultiLineString(jtsLines, geomFactory))
  }

  def apply(lines: Array[Line], srid: Int): MultiLine = {
    val jtsLines = lines.map(l => l.jtsGeometry)
    val gf = new jts.GeometryFactory(null, srid)
    MultiLine(new jts.MultiLineString(jtsLines, gf))
  }

  implicit def jtsToMultiLine(jtsGeom: jts.MultiLineString): MultiLine = {
    apply(jtsGeom)
  }

}

case class MultiLine(jtsGeometry: jts.MultiLineString) extends GeometryCollection {

  def reverse: MultiLine = {
    MultiLine(jtsGeometry.reverse.asInstanceOf[jts.MultiLineString])
  }

}
