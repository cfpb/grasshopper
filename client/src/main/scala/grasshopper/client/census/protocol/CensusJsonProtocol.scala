package grasshopper.client.census.protocol

import spray.json.DefaultJsonProtocol
import grasshopper.client.census.model.{ CensusResult, CensusStatus }
import io.geojson.FeatureJsonProtocol._

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val censusStatusFormat = jsonFormat4(CensusStatus.apply)
  implicit val censusResultFormat = jsonFormat1(CensusResult.apply)
}
