package grasshopper.geometry

import com.vividsolutions.jts.{ geom => jts }

trait GeometryCollection extends Geometry {

  def geometryAt(n: Int): jts.Geometry = jtsGeometry.getGeometryN(n)

  def numGeometries: Int = jtsGeometry.getNumGeometries

  def area = {
    jtsGeometry.getArea
  }

  def geometries: List[jts.Geometry] = {
    def loop(list: List[jts.Geometry], n: Int): List[jts.Geometry] = n match {
      case 0 => list
      case _ => loop(jtsGeometry.getGeometryN(n - 1) :: list, n - 1)
    }
    loop(Nil, numGeometries)
  }

}
