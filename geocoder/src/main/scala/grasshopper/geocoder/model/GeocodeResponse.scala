package grasshopper.geocoder.model

import feature.Feature
import grasshopper.client.parser.model.ParsedAddress

object GeocodeResponse {
  def empty = GeocodeResponse("", ParsedAddress.empty, Nil.toArray)
}
case class GeocodeResponse(status: String, query: ParsedAddress, features: Array[Feature])
