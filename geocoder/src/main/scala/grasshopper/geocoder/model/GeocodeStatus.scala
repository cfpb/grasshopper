package grasshopper.geocoder.model

import grasshopper.client.addresspoints.model.AddressPointsStatus
import grasshopper.client.census.model.CensusStatus
import grasshopper.client.parser.model.ParserStatus

case class GeocodeStatus(addressPointsStatus: AddressPointsStatus, censusStatus: CensusStatus, parserStatus: ParserStatus)
