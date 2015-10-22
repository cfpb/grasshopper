package grasshopper.protocol

import grasshopper.model.Status
import spray.json.DefaultJsonProtocol

trait StatusJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
}
