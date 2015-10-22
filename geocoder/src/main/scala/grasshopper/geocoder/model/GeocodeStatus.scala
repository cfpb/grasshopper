package grasshopper.geocoder.model

import grasshopper.model.Status
import grasshopper.client.parser.model.ParserStatus

case class GeocodeStatus(addressPointsStatus: Status, censusStatus: Status, parserStatus: ParserStatus)
