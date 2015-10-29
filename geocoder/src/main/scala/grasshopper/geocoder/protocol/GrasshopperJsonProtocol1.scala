package grasshopper.geocoder.protocol

import grasshopper.client.parser.protocol.ParserJsonProtocol
import grasshopper.geocoder.model.GeocodeStatus1
import grasshopper.protocol.StatusJsonProtocol

trait GrasshopperJsonProtocol1 extends StatusJsonProtocol with ParserJsonProtocol {
  implicit val geocodeStatus1Format = jsonFormat1(GeocodeStatus1.apply)
}
