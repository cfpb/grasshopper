package grasshopper.addresspoints.protocol

import grasshopper.addresspoints.model.{ AddressInput, Status }
import spray.json.DefaultJsonProtocol

trait AddressPointJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val addressInputFormat = jsonFormat1(AddressInput.apply)
}