package grasshopper.geocoder.model

sealed trait BatchGeocodeResult
case class AddressPointsGeocodeBatchResult(input: String, latitude: Double, longitude: Double) extends BatchGeocodeResult {
  def toCsv = s"${input},${latitude},${longitude}\n"
}
case class CensusGeocodeBatchResult(input: String, latitude: Double, longitude: Double) extends BatchGeocodeResult {
  def toCsv = s"${input},${latitude},${longitude}\n"
}
