package grasshopper.protocol

import grasshopper.model.AddressSearchResult
import io.geojson.FeatureJsonProtocol._
import spray.json.DefaultJsonProtocol

trait AddressSearchJsonProtocol extends DefaultJsonProtocol {
  implicit val addressSearchFormat = jsonFormat1(AddressSearchResult.apply)
}