package grasshopper.geometry

import com.vividsolutions.jts.{ geom => jts }
import jts.Coordinate

object Util {

  def points2JTSCoordinates(points: Traversable[Point]): Traversable[Coordinate] = {
    points.map { point =>
      new Coordinate(point.x, point.y, point.z)
    }
  }

}
