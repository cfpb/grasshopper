package grasshopper.geocoder.model

import grasshopper.client.parser.model.ParsedAddress
import grasshopper.model.addresspoints.AddressPointsResult
import grasshopper.model.census.AddressSearchResult$

case class GeocodeResult(status: String, input: String, query: ParsedAddress, addressPointsService: AddressPointsResult, censusService: AddressSearchResult)
