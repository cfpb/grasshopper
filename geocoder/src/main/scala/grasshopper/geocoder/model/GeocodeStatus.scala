package grasshopper.geocoder.model

import grasshopper.client.addresspoints.model.AddressPointsStatus
import grasshopper.model.Status
import grasshopper.client.parser.model.ParserStatus

case class GeocodeStatus(addressPointsStatus: AddressPointsStatus, censusStatus: Status, parserStatus: ParserStatus)
