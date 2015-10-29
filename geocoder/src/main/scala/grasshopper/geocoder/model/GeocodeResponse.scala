package grasshopper.geocoder.model

import feature.Feature
import grasshopper.model.census.ParsedInputAddress

case class GeocodeResponse(status: String, query: ParsedInputAddress, features: Array[Feature])
