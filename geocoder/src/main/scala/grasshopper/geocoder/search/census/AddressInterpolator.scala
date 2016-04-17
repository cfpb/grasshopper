package grasshopper.geocoder.search.census

import com.typesafe.scalalogging.Logger
import feature._
import geometry._
import grasshopper.geocoder.search.census.SearchUtils._
import grasshopper.model.census.AddressRange
import org.slf4j.LoggerFactory

object AddressInterpolator {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census-addressinterpolator"))

  def calculateAddressRange(feature: Feature, addressNumber: Int): AddressRange = {
    val pre: String = prefix(feature, addressNumber)

    val s = feature.values.getOrElse(s"${pre}FROMHN", "0")
    val e = feature.values.getOrElse(s"${pre}TOHN", "0")

    val start = s match {
      case "" => 0
      case _ => toInt(s.toString).getOrElse(0)
    }
    val end = e match {
      case "" => 0
      case _ => toInt(e.toString).getOrElse(0)
    }

    AddressRange(start, end)
  }

  def interpolate(feature: Feature, range: AddressRange, addressNumber: Int): Feature = {
    val numberIsEven = addressNumber % 2 == 0
    val addressRangeIsEven = range.end % 2 == 0 && range.start % 2 == 0
    val addressField = Field("address", StringType())
    val geomField = Field("geometry", GeometryType())
    val numberField = Field("number", IntType())
    val schema = Schema(geomField, addressField, numberField)

    if ((addressRangeIsEven && numberIsEven) || (!addressRangeIsEven && !numberIsEven)) {
      val pre = prefix(feature, addressNumber)
      val sign = if (pre == "R") -1 else 1
      val line = feature.geometry.asInstanceOf[Line]
      val l = line.length
      val d = calculateDistance(range)
      val x = addressNumber - range.start
      val dist = x * l / d
      val geometry = line.pointAtDistWithOffset(dist, sign * 0.0001)

      val fullname = feature.values.getOrElse("FULLNAME", "")
      val zipL = feature.values.getOrElse("ZIPL", "")
      val zipR = feature.values.getOrElse("ZIPR", "")
      val lfromhn = feature.values.getOrElse("LFROMHN", "")
      val ltohn = feature.values.getOrElse("LTOHN", "")
      val rfromhn = feature.values.getOrElse("RFROMHN", "")
      val rtohn = feature.values.getOrElse("RTOHN", "")
      val state = feature.values.getOrElse("STATE", "")
      val values: Map[String, Any] = Map(
        "geometry" -> geometry,
        "FULLNAME" -> fullname,
        "RFROMHN" -> rfromhn,
        "RTOHN" -> rtohn,
        "LFROMHN" -> lfromhn,
        "LTOHN" -> ltohn,
        "ZIPR" -> zipR,
        "ZIPL" -> zipL,
        "STATE" -> state
      )
      Feature(schema, values)
    } else {
      val values: Map[String, Any] = Map.empty
      Feature(schema, values)
    }

  }

  private def prefix(feature: Feature, addressNumber: Int): String = {
    val addressIsEven = addressNumber % 2 == 0
    val rightRangeIsEven: Boolean = rangeIsEven(feature, false)

    val leftRangeIsEven: Boolean = rangeIsEven(feature, true)

    if (addressIsEven && rightRangeIsEven) {
      "R"
    } else if (addressIsEven && leftRangeIsEven) {
      "L"
    } else if (!addressIsEven && !rightRangeIsEven) {
      "R"
    } else if (!addressIsEven && !leftRangeIsEven) {
      "L"
    } else {
      val default = "R"
      log.warn(s"Could not determine R/L prefix.  Defaulting to $default.  Feature: ${feature.values}")
      default
    }
  }

  private def rangeIsEven(f: Feature, isLeft: Boolean): Boolean = {
    val prefix = if (isLeft) "L" else "R"
    val fromEven = f.values.getOrElse(s"${prefix}FROMHN", 0)
    val fromRange = fromEven match {
      case "" => 0
      case _ => toInt(fromEven.toString).getOrElse(0)
    }
    val fromIsEven = fromEven != "" && fromRange % 2 == 0

    val toEven = f.values.getOrElse(s"${prefix}TOHN", 0)
    val toRange = fromEven match {
      case "" => 0
      case _ => toInt(toEven.toString).getOrElse(0)
    }
    val toIsEven = toEven != "" && toRange % 2 == 0
    fromIsEven || toIsEven
  }

  private def calculateDistance(range: AddressRange): Double = {
    val start = range.start
    val end = range.end
    if (start < end)
      end - start
    else
      start - end
  }
}
