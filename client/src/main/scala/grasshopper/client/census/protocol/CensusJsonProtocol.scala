package grasshopper.client.census.protocol

import grasshopper.client.census.model.CensusStatus
import spray.json.DefaultJsonProtocol

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val censusStatusFormat = jsonFormat4(CensusStatus.apply)
}
