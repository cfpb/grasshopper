package grasshopper.census.util

import geometry._
import feature._
import spray.json._
import io.geojson.FeatureJsonProtocol._

object TestData {

  def getTigerLine1(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line1.geojson").getLines.mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getTigerLine2(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line2.geojson").getLines().mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getTigerLine3(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line3.geojson").getLines().mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getTigerLine4(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line4.geojson").getLines().mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getTigerLine5(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line5.geojson").getLines().mkString
    fjson.parseJson.convertTo[Feature]
  }

  def emptyFeature(): Feature = {
    val p = Point(0, 0)
    val values = Map("geometry" -> p, "desc" -> "empty")

    val schema = Schema(List(
      Field("geometry", GeometryType()),
      Field("desc", StringType())
    ))
    Feature(schema, values)
  }

  def emptyFeatures(): Array[Feature] = {
    List(emptyFeature).toArray
  }

}

