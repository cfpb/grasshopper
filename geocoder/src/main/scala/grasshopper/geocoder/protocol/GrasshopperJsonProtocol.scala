package grasshopper.geocoder.protocol

import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.client.protocol.ClientJsonProtocol
import grasshopper.geocoder.model._
import grasshopper.protocol.StatusJsonProtocol
import grasshopper.protocol.addresspoints.AddressPointsJsonProtocol
import grasshopper.protocol.census.CensusJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol
    extends ClientJsonProtocol
    with AddressPointsJsonProtocol
    with CensusJsonProtocol
    with ParserJsonProtocol
    with StatusJsonProtocol {

  implicit val geocodeResponseFormat = jsonFormat2(GeocodeResponse.apply)
  implicit val geocodeResultFormat = jsonFormat5(GeocodeResult.apply)
  implicit val geocodeStatusFormat = jsonFormat3(GeocodeStatus.apply)
  implicit val addressPointsGeocodeBatchResultFormat = jsonFormat3(AddressPointsGeocodeBatchResult.apply)
  implicit val censusGeocodeBatchResultFormat = jsonFormat3(CensusGeocodeBatchResult.apply)
}
