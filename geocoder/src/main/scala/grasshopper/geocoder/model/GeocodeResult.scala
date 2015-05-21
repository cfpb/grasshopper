package grasshopper.geocoder.model

import grasshopper.client.addresspoints.model.AddressPointsResult
import grasshopper.client.census.model.CensusResult
import grasshopper.client.parser.model.ParsedAddress

case class GeocodeResult(status: String, query: ParsedAddress, addressPointsService: AddressPointsResult, censusService: CensusResult)
