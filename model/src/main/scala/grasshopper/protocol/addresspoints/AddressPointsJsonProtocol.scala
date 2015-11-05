package grasshopper.protocol.addresspoints

import grasshopper.model.addresspoints.{ AddressPointsResult, AddressInput }
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait AddressPointsJsonProtocol extends DefaultJsonProtocol {
  implicit val addressInputFormat = jsonFormat1(AddressInput.apply)
  implicit val addressPointResult = jsonFormat3(AddressPointsResult.apply)
}