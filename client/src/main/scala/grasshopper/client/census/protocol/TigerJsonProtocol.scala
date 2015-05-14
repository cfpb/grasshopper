package grasshopper.client.census.protocol

import spray.json.DefaultJsonProtocol
import grasshopper.client.census.model.CensusStatus

trait TigerJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(CensusStatus.apply)
}
