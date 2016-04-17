package grasshopper.geocoder.search.census

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object SearchUtils {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census-searchutils"))

  def toInt(s: String): Option[Int] = {
    try {
      if (isNumeric(s)) {
        Some(s.toInt)
      } else {
        if (s.contains("-")) {
          Some(s.substring(0, s.indexOf("-")).toInt)
        } else if (s == "None") {
          None
        } else {
          Some(s.replaceAll("[^\\d]", "").toInt)
        }
      }
    } catch {
      case e: Exception =>
        log.error(s"Could not parse integer from address number '$s'")
        None
    }
  }

  def isNumeric(str: String): Boolean = {
    str.matches("-?\\d+(\\.\\d+)?")
  }
}
