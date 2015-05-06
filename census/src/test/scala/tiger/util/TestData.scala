package tiger.util

import geometry._
import feature._
import spray.json._
import io.geojson.FeatureJsonProtocol._

object TestData {

  def getTigerLine1(): Feature = {
    val fjson = scala.io.Source.fromFile("census/src/test/resources/tiger_line1.geojson").getLines.mkString
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

