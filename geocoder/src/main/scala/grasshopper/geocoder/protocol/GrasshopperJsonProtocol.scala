package grasshopper.geocoder.protocol

import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.geocoder.model.{ GeocodeResult, GeocodeResponse, GeocodeStatus }
import grasshopper.protocol.{ AddressSearchJsonProtocol, StatusJsonProtocol }
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol
    extends StatusJsonProtocol
    with ParserJsonProtocol
    with AddressPointsJsonProtocol
    with AddressSearchJsonProtocol {

  implicit val geocodeStatus1Format = jsonFormat1(GeocodeStatus.apply)
  implicit val geocodeResponseFormat = jsonFormat2(GeocodeResponse.apply)
  implicit val geocodeResultFormat = jsonFormat5(GeocodeResult.apply)
}
