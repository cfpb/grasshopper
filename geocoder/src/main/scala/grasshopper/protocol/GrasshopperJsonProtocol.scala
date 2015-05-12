package grasshopper.protocol

import grasshopper.model.Status
import spray.json.DefaultJsonProtocol

trait GrasshopperJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
}
