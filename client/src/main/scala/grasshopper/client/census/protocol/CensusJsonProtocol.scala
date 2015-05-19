package grasshopper.client.census.protocol

import spray.json.DefaultJsonProtocol
import grasshopper.client.census.model.CensusStatus

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val censusStatusFormat = jsonFormat4(CensusStatus.apply)
}
