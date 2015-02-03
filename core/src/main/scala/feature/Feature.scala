package grasshopper.feature

import com.vividsolutions.jts.{ geom => jts }
import grasshopper.geometry._

object Feature {

  def apply(geometry: Geometry): Feature = {
    val values = Map("geometry" -> geometry)
    Feature(values)
  }

  def apply(geometry: Geometry, properties: Map[String, Any]): Feature = {
    val values = properties.updated("geometry", geometry)
    Feature(values)
  }

}

case class Feature(values: Map[String, Any]) {

  val countFields = values.size

  def addOrUpdate(k: String, v: Any): Feature = Feature(values.updated(k, v))

  def updateGeometry(geom: Geometry) = Feature(values.updated("geometry", geom))

  def get(k: String): Option[Any] = values.get(k)

  def geometry: Geometry = get("geometry") match {
    case Some(g) => g.asInstanceOf[Geometry]
    case None => Point(0, 0)
  }

  def envelope: Envelope = geometry.envelope
}
