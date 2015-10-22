package grasshopper.client.protocol

import grasshopper.client.census.model.CensusResult
import grasshopper.client.model.ResponseError
import grasshopper.model.addresspoints.AddressPointsResult
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait ClientJsonProtocol extends DefaultJsonProtocol {
  implicit val responseErrorFormat = jsonFormat1(ResponseError.apply)
}
