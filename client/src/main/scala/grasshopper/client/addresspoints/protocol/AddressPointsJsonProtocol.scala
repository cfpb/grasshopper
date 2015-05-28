package grasshopper.client.addresspoints.protocol

import grasshopper.client.addresspoints.model._
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait AddressPointsJsonProtocol extends DefaultJsonProtocol {
  implicit val addressPointsStatusFormat = jsonFormat4(AddressPointsStatus.apply)
  implicit val addressPointsResultFormat = jsonFormat2(AddressPointsResult.apply)
}
