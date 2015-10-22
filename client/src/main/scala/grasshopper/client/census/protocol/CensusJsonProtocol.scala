package grasshopper.client.census.protocol

import grasshopper.model.Status
import grasshopper.client.census.model.CensusResult
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val censusStatusFormat = jsonFormat4(Status.apply)
  implicit val censusResultFormat = jsonFormat2(CensusResult.apply)
}
