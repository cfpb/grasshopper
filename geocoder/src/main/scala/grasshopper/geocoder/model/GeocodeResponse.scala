package grasshopper.geocoder.model

import feature.Feature
import grasshopper.client.parser.model.{ AddressPart }

object GeocodeResponse {
  def empty: GeocodeResponse = GeocodeResponse("", List.empty, List.empty)
}

case class GeocodeResponse(input: String, parts: List[AddressPart], features: List[Feature])
