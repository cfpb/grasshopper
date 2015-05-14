package grasshopper.client.census

import com.typesafe.config.{ ConfigFactory, Config }
import grasshopper.client.ServiceClient

object CensusClient extends ServiceClient {
  override val config: Config = ConfigFactory.load()

}
