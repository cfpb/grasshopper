package grasshopper.geocoder.protocol

import grasshopper.geocoder.model._
import spray.json.DefaultJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val addressPartFormat = jsonFormat6(AddressPart.apply)
  implicit val parsedAddressFormat = jsonFormat2(ParsedAddress.apply)
  implicit val serviceResult = jsonFormat2(ServiceResult.apply)
  implicit val geocodeResultFormat = jsonFormat3(GeocodeResult.apply)
}
