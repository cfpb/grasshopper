package grasshopper.geocoder.model

import feature.Feature
import grasshopper.client.parser.model.ParsedAddress

object GeocodeResponse {
  def empty = GeocodeResponse(ParsedAddress.empty, Nil)
}
case class GeocodeResponse(query: ParsedAddress, features: List[Feature])
