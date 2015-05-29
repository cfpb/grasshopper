package grasshopper.geocoder.protocol

import grasshopper.client.addresspoints.protocol.AddressPointsJsonProtocol
import grasshopper.client.census.protocol.CensusJsonProtocol
import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.client.protocol.ClientJsonProtocol
import grasshopper.geocoder.model._

trait GrasshopperJsonProtocol extends ClientJsonProtocol with AddressPointsJsonProtocol with CensusJsonProtocol with ParserJsonProtocol {
  implicit val geocodeResultFormat = jsonFormat4(GeocodeResult.apply)
  implicit val geocodeStatusFormat = jsonFormat3(GeocodeStatus.apply)
}
