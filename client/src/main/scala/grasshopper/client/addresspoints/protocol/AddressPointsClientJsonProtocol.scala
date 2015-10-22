package grasshopper.client.addresspoints.protocol

import grasshopper.client.addresspoints.model._
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait AddressPointsClientJsonProtocol extends DefaultJsonProtocol {
  implicit val addressPointsResultFormat = jsonFormat2(AddressPointsResult.apply)
}
