package grasshopper.geocoder.model

sealed trait BatchGeocodeResult
case class AddressPointsGeocodeBatchResult(input: String, latitude: Double, longitude: Double) extends BatchGeocodeResult
case class CensusGeocodeBatchResult(input: String, latitude: Double, longitude: Double) extends BatchGeocodeResult
