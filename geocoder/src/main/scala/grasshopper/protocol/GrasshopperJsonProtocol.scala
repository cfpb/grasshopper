package grasshopper.protocol

import grasshopper.model.ParserStatus
import spray.json.DefaultJsonProtocol

trait GrasshopperJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(ParserStatus.apply)
}
