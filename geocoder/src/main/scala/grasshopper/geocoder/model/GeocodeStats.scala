package grasshopper.geocoder.model

import feature.Feature

case class GeocodeStats(total: Int, parsed: Int, points: Int, census: Int, geocoded: Int, features: List[Feature])
