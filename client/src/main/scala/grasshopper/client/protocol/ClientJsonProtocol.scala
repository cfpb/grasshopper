package grasshopper.client.protocol

import grasshopper.client.addresspoints.model.AddressPointsResult
import grasshopper.client.census.model.CensusResult
import grasshopper.client.model.ResponseError
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait ClientJsonProtocol extends DefaultJsonProtocol {
  implicit val responseErrorFormat = jsonFormat1(ResponseError.apply)
  implicit val addressPointsResultFormat = jsonFormat1(AddressPointsResult.apply)
  implicit val censusResultFormat = jsonFormat1(CensusResult.apply)
}
