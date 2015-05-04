package tiger.search

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait CensusGeocode {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-geocode"))
}
