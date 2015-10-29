package grasshopper.protocol.census

import grasshopper.model.census.{ CensusResult, ParsedInputAddress }
import io.geojson.FeatureJsonProtocol._
import spray.json.DefaultJsonProtocol

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val parsedAddressInputFormat = jsonFormat5(ParsedInputAddress.apply)
  implicit val censusResultFormat = jsonFormat2(CensusResult.apply)
}