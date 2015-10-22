package grasshopper.protocol.census

import grasshopper.model.census.{ CensusResult, ParsedInputAddress, Status }
import io.geojson.FeatureJsonProtocol._
import spray.json.DefaultJsonProtocol

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val addressInputFormat = jsonFormat4(ParsedInputAddress.apply)
  implicit val censusResultFormat = jsonFormat2(CensusResult.apply)
}