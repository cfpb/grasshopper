package grasshopper.geojson

import com.vividsolutions.jts.{ geom => jts }
import spray.json._
import grasshopper.geometry._
import grasshopper.feature._

object GeometryJsonProtocol extends DefaultJsonProtocol with NullOptions {

  implicit object PointFormat extends RootJsonFormat[Point] {
    def write(p: Point): JsValue = {
      JsObject(
        "type" -> JsString("Point"),
        "coordinates" -> JsArray(JsNumber(p.x), JsNumber(p.y), JsNumber(p.z))
      )
    }

    def read(json: JsValue): Point = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("Point"), JsArray(Vector(JsNumber(x), JsNumber(y), JsNumber(z)))) =>
          Point(x.toDouble, y.toDouble, z.toDouble)
        case Seq(JsString("Point"), JsArray(Vector(JsNumber(x), JsNumber(y)))) =>
          Point(x.toDouble, y.toDouble)
        case _ => throw new DeserializationException("Point GeoJSON expected")
      }
    }
  }

  implicit object LineFormat extends RootJsonFormat[Line] {
    def write(l: Line): JsValue = {
      toCoords(l.points.toVector, "LineString")
    }

    def read(json: JsValue): Line = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("LineString"), JsArray(p)) =>
          val points = toPoints(p)
          Line(points)
        case _ => throw new DeserializationException("LineString GeoJSON expected")
      }
    }
  }

  implicit object PolygonFormat extends RootJsonFormat[Polygon] {
    def write(p: Polygon): JsValue = {
      val pext = p.boundary.points
      val holes = p.holes
      val ptsExt = pext.map { k =>
        JsArray(JsNumber(k.x), JsNumber(k.y), JsNumber(k.z))
      }
      p.jtsGeometry.getNumInteriorRing match {
        case 0 =>
          JsObject(
            "type" -> JsString("Polygon"),
            "coordinates" -> JsArray(JsArray(ptsExt.toVector))
          )
        case _ =>
          val boundary = p.boundary.jtsGeometry
          val holes = p.holes.map(h => h.jtsGeometry).toList
          val geometries = boundary :: holes
          toCoords(geometries, "Polygon")
      }

    }
    def read(json: JsValue): Polygon = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("Polygon"), JsArray(p)) =>
          val lines = toLines(p).toList
          Polygon(lines.head, lines.tail.toArray)
        case _ => throw new DeserializationException("Polygon GeoJSON expected")
      }
    }
  }

  implicit object MultiPointFormat extends RootJsonFormat[MultiPoint] {
    def write(p: MultiPoint): JsValue = {
      toCoords(p.points.toVector, "MultiPoint")
    }
    def read(json: JsValue): MultiPoint = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("MultiPoint"), JsArray(p)) =>
          val points = toPoints(p)
          MultiPoint(points)
        case _ => throw new DeserializationException("MultiPoint GeoJSON expected")
      }
    }
  }

  implicit object MultiLineFormat extends RootJsonFormat[MultiLine] {
    def write(ml: MultiLine): JsValue = {
      val lines = ml.geometries
      toCoords(lines, "MultiLineString")
    }
    def read(json: JsValue): MultiLine = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("MultiLineString"), JsArray(l)) =>
          val lines = toLines(l)
          MultiLine(lines.toArray)
        case _ => throw new DeserializationException("MultiLineString GeoJSON expected")
      }
    }
  }

  implicit object MultiPolygonFormat extends RootJsonFormat[MultiPolygon] {
    def write(p: MultiPolygon): JsValue = {
      val rings = p.geometries
      toMultiCoords(rings, "MultiPolygon")
    }
    def read(json: JsValue): MultiPolygon = {
      json.asJsObject.getFields("type", "coordinates") match {
        case Seq(JsString("MultiPolygon"), JsArray(polys)) =>
          val polygons = toPolygons(polys)
          MultiPolygon(polygons.toArray)
        case _ => throw new DeserializationException("MultiPolygon GeoJSON expected")
      }
    }
  }

  private def toPoints(coords: Vector[JsValue]): Vector[Point] = {
    coords.map { x =>
      val point = JsObject(
        "type" -> JsString("Point"),
        "coordinates" -> x
      )
      point.convertTo[Point]
    }
  }

  private def toLines(coords: Vector[JsValue]): Vector[Line] = {
    coords.map { x =>
      val line = JsObject(
        "type" -> JsString("LineString"),
        "coordinates" -> x
      )
      line.convertTo[Line]
    }
  }

  private def toPolygons(polys: Vector[JsValue]): Vector[Polygon] = {
    polys(0) match {
      case polyArr: JsArray =>
        polyArr.elements.map { x =>
          val points = x match {
            case ptArr: JsArray =>
              ptArr.elements.flatMap { p =>
                toPoints(Vector(p))
              }
            case _ => Nil
          }
          Polygon(points)
        }
      case _ => Vector()
    }
  }

  private def toCoords(points: Vector[Point], `type`: String): JsValue = {
    val coords = points.map { p =>
      JsArray(JsNumber(p.x), JsNumber(p.y), JsNumber(p.z))
    }.toVector
    JsObject(
      "type" -> JsString(`type`),
      "coordinates" -> JsArray(coords)
    )
  }

  private def toCoords(geometries: List[jts.Geometry], `type`: String): JsValue = {
    JsObject(
      "type" -> JsString(`type`),
      "coordinates" -> JsArray(
        geometries.map { g =>
        JsArray(
          g.getCoordinates.map { c =>
          JsArray(JsNumber(c.x), JsNumber(c.y), JsNumber(c.z))
        }.toVector
        )
      }.toVector
      )
    )
  }

  private def toMultiCoords(geometries: List[jts.Geometry], `type`: String): JsValue = {
    JsObject(
      "type" -> JsString(`type`),
      "coordinates" -> JsArray(
        JsArray(
          geometries.map { g =>
          JsArray(
            g.getCoordinates.map { c =>
            JsArray(JsNumber(c.x), JsNumber(c.y), JsNumber(c.z))
          }.toVector
          )
        }.toVector
        )
      )
    )
  }

}
