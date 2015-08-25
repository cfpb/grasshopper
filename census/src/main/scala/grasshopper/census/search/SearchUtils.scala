package grasshopper.census.search

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object SearchUtils {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-census-searchutils"))

  def toInt(s: String): Option[Int] = {
    try {
      if (isNumeric(s)) {
        Some(s.toInt)
      } else {
        Some(s.replaceAll("[^\\d.]", "").toInt)
      }
    } catch {
      case e: Exception =>
        log.error(e.getMessage)
        None
    }
  }

  private def isNumeric(str: String): Boolean = {
    str.matches("-?\\d+(\\.\\d+)?")
  }
}
