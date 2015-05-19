package grasshopper.geocoder.protocol

import grasshopper.client.addresspoints.protocol.AddressPointsJsonProtocol
import grasshopper.client.census.protocol.CensusJsonProtocol
import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.geocoder.model._
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol extends AddressPointsJsonProtocol with CensusJsonProtocol with ParserJsonProtocol {
  implicit val serviceResult = jsonFormat2(ServiceResult.apply)
  implicit val geocodeResultFormat = jsonFormat3(GeocodeResult.apply)
  implicit val geocodeStatusFormat = jsonFormat3(GeocodeStatus.apply)
}
