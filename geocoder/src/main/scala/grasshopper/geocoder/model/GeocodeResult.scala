package grasshopper.geocoder.model

import feature.Feature

case class ServiceResult(service: String, data: Feature)
case class GeocodeResult(status: String, query: ParsedAddress, features: List[ServiceResult])
