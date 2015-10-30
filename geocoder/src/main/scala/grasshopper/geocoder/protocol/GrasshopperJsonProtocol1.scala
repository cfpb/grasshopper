package grasshopper.geocoder.protocol

import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.geocoder.model.{ GeocodeResponse, GeocodeStatus1 }
import grasshopper.protocol.StatusJsonProtocol
import io.geojson.FeatureJsonProtocol._

trait GrasshopperJsonProtocol1 extends StatusJsonProtocol with ParserJsonProtocol {
  implicit val geocodeStatus1Format = jsonFormat1(GeocodeStatus1.apply)
  implicit val geocodeResponseFormat = jsonFormat2(GeocodeResponse.apply)
}
