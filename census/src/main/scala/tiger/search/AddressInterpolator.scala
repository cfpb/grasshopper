package tiger.search

import geometry._
import feature._
import io.geojson.FeatureJsonProtocol._
import tiger.model.AddressRange

object AddressInterpolator {

  def calculateAddressRange(f: Feature, a: Int): AddressRange = {
    val addressIsEven = (a % 2 == 0)
    val rightRangeIsEven = (f.values.getOrElse("RFROMHN", 0).toString.toInt % 2 == 0)
    val leftRangeIsEven = (f.values.getOrElse("LFROMHN", 0).toString.toInt % 2 == 0)

    val prefix =
      if (addressIsEven && rightRangeIsEven)
        "R"
      else if (addressIsEven && leftRangeIsEven)
        "L"
      else if (!addressIsEven && !rightRangeIsEven)
        "R"
      else if (!addressIsEven && !leftRangeIsEven)
        "L"

    val start = f.values.getOrElse(s"${prefix}FROMHN", "0").toString.toInt
    val end = f.values.getOrElse(s"${prefix}TOHN", "0").toString.toInt

    AddressRange(start, end)
  }

  def interpolate(feature: Feature, range: AddressRange, a: Int): Feature = {
    val sign = if (a % 2 == 0) 1 else -1
    val line = feature.geometry.asInstanceOf[Line]
    val l = line.length
    val d = range.end - range.start
    val x = a - range.start
    val dist = x * l / d
    //TODO: Review how offset is being calculated
    val geometry = line.pointAtDistWithOffset((dist * -1), sign * 0.0001)
    val addressField = Field("address", StringType())
    val geomField = Field("geometry", GeometryType())
    val numberField = Field("number", IntType())
    val schema = Schema(geomField, addressField, numberField)
    val fullname = feature.values.get("FULLNAME").getOrElse("")
    val address = a.toString + " " + fullname
    val values: Map[String, Any] = Map("geometry" -> geometry, "address" -> address, "number" -> a)
    Feature(schema, values)
  }
}
