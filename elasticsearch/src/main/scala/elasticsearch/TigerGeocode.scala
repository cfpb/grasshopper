package elasticsearch

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait TigerGeocode {

  lazy val log = Logger(LoggerFactory.getLogger("grasshopper-geocode"))
}
