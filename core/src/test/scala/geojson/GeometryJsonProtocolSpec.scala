package grasshopper.geojson

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.Checkers
import grasshopper.geometry._
import grasshopper.feature._
import spray.json._

class GeometryJsonProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  import grasshopper.geojson.GeometryJsonProtocol._

  val p1 = Point(-77.1, 38.5)
  val p2 = Point(-102.2, 45.8)
  val p3 = Point(-85.1, 39.1)
  val p4 = Point(-78, 32)
  val p5 = Point(-77, 32)
  val p6 = Point(-76, 35)
  val l1 = Line(p1, p2)
  val l2 = Line(p2, p3)
  val poly1 = Polygon(p1, p2, p3, p1)
  val poly2 = Polygon(p4, p5, p6, p4)
  val mp = MultiPoint(p1, p2)
  val ml = MultiLine(l1, l2)
  val mpoly = MultiPolygon(poly1, poly2)
  val ph1 = Point(100, 0)
  val ph2 = Point(101, 0)
  val ph3 = Point(101, 1)
  val ph4 = Point(100, 1)
  val h1 = Point(100.2, 0.2)
  val h2 = Point(100.8, 0.2)
  val h3 = Point(100.8, 0.8)
  val exterior = Line(ph1, ph2, ph3, ph4, ph1)
  val hole = Line(h1, h2, h3, h1)
  val polyWithHole = Polygon(exterior, hole)
  val pointJson = """{"type":"Point","coordinates":[-77.1,38.5,0.0]}"""
  val lineJson = """{"type":"LineString","coordinates":[[-77.1,38.5,0.0],[-102.2,45.8,0.0]]}"""
  val polyJson = """{"type":"Polygon","coordinates":[[[-77.1,38.5,0.0],[-102.2,45.8,0.0],[-85.1,39.1,0.0],[-77.1,38.5,0.0]]]}"""
  val polyWithHoleJson = """{"type":"Polygon","coordinates":[[[100.0,0.0,0.0],[101.0,0.0,0.0],[101.0,1.0,0.0],[100.0,1.0,0.0],[100.0,0.0,0.0]],[[100.2,0.2,0.0],[100.8,0.2,0.0],[100.8,0.8,0.0],[100.2,0.2,0.0]]]}"""
  val mpJson = """{"type":"MultiPoint","coordinates":[[-77.1,38.5,0.0],[-102.2,45.8,0.0]]}"""
  val mlJson = """{"type":"MultiLineString","coordinates":[[[-77.1,38.5,0.0],[-102.2,45.8,0.0]],[[-102.2,45.8,0.0],[-85.1,39.1,0.0]]]}"""
  val mpolyJson = """{"type":"MultiPolygon","coordinates":[[[[-77.1,38.5,0.0],[-102.2,45.8,0.0],[-85.1,39.1,0.0],[-77.1,38.5,0.0]],[[-78.0,32.0,0.0],[-77.0,32.0,0.0],[-76.0,35.0,0.0],[-78.0,32.0,0.0]]]]}"""

  property("Point must write to GeoJSON") {
    p1.toJson.toString mustBe (pointJson)
  }

  property("GeoJSON must read into Point") {
    p1.toJson.convertTo[Point] must be(p1)
  }

  property("Line must write to GeoJSON") {
    l1.toJson.toString must be(lineJson)
  }

  property("GeoJSON must read into Line") {
    l1.toJson.convertTo[Line] must be(l1)
  }

  property("Polygon must write to GeoJSON") {
    poly1.toJson.toString must be(polyJson)
  }

  property("GeoJSON must read into Polygon") {
    poly1.toJson.convertTo[Polygon] must be(poly1)
  }

  property("Polygon with hole must write to GeoJSON") {
    polyWithHole.toJson.toString must be(polyWithHoleJson)
  }

  property("GeoJSON must read into Polygon with hole") {
    polyWithHole.toJson.convertTo[Polygon] must be(polyWithHole)
  }

  property("MultiPoint must write to GeoJSON") {
    mp.toJson.toString must be(mpJson)
  }

  property("GeoJSON must read into MultiPoint") {
    mp.toJson.convertTo[MultiPoint] must be(mp)
  }

  property("MultiLine must write to GeoJSON") {
    ml.toJson.toString must be(mlJson)
  }

  property("GeoJSON must read into MultiLine") {
    ml.toJson.convertTo[MultiLine] must be(ml)
  }

  property("MultiPolygon must write to GeoJSON") {
    mpoly.toJson.toString must be(mpolyJson)
  }

  property("GeoJSON must read into MultiPolygon") {
    mpoly.toJson.convertTo[MultiPolygon] must be(mpoly)
  }

}
