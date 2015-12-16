package grasshopper.geocoder.protocol

import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStatus, GeocodeStats }
import grasshopper.protocol.{ AddressSearchJsonProtocol, StatusJsonProtocol }
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol
    extends StatusJsonProtocol
    with ParserJsonProtocol
    with AddressSearchJsonProtocol {

  implicit val geocodeStatusFormat = jsonFormat1(GeocodeStatus.apply)
  implicit val geocodeResponseFormat = jsonFormat3(GeocodeResponse.apply)
  implicit val geocodeStatsFormat = jsonFormat6(GeocodeStats.apply)
}
