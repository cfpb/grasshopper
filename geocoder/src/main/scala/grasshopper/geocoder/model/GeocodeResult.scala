package grasshopper.geocoder.model

import grasshopper.client.census.model.CensusResult
import grasshopper.client.parser.model.ParsedAddress
import grasshopper.model.addresspoints.AddressPointsResult

case class GeocodeResult(status: String, input: String, query: ParsedAddress, addressPointsService: AddressPointsResult, censusService: CensusResult)
