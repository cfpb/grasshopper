package grasshopper.client.census.protocol

import grasshopper.client.census.model.{ CensusResult, CensusStatus }
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val censusStatusFormat = jsonFormat4(CensusStatus.apply)
  implicit val censusResultFormat = jsonFormat2(CensusResult.apply)
}
