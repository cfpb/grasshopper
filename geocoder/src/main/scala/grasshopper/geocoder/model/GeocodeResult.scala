package grasshopper.geocoder.model

import feature.Feature
import grasshopper.client.parser.model.ParsedAddress

case class ServiceResult(service: String, data: List[Feature])
case class GeocodeResult(status: String, query: ParsedAddress, features: List[ServiceResult])
