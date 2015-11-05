package grasshopper.geocoder.util

import feature.Feature
import spray.json._
import io.geojson.FeatureJsonProtocol._

object TestData {

  def getPointFeature1: Feature = {
    val fjson = scala.io.Source.fromFile("geocoder/src/test/resources/address_point1.geojson").getLines.mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getPointFeature2: Feature = {
    val fjson = scala.io.Source.fromFile("geocoder/src/test/resources/address_point2.geojson").getLines.mkString
    fjson.parseJson.convertTo[Feature]
  }

  def getTigerLine1: Feature = {
    val fjson = scala.io.Source.fromFile("geocoder/src/test/resources/tiger_line1.geojson").getLines.mkString
    fjson.parseJson.convertTo[Feature]
  }

}
