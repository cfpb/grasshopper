package grasshopper.protocol.addresspoints

import grasshopper.model.Status
import grasshopper.model.addresspoints.{ AddressInput, AddressPointsResult }
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait AddressPointJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val addressInputFormat = jsonFormat1(AddressInput.apply)
  implicit val addressPointResult = jsonFormat3(AddressPointsResult.apply)
}