package grasshopper.client.addresspoints.protocol

import grasshopper.client.addresspoints.model._
import spray.json.DefaultJsonProtocol

trait AddressPointsJsonProtocol extends DefaultJsonProtocol {
  implicit val addressPointsStatusFormat = jsonFormat4(AddressPointsStatus.apply)
}
