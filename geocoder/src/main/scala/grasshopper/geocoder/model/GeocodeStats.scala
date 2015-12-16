package grasshopper.geocoder.model

import feature.FeatureCollection

case class GeocodeStats(total: Int, parsed: Int, points: Int, census: Int, geocoded: Int, fc: FeatureCollection)
