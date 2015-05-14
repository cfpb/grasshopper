package grasshopper.census.protocol

import grasshopper.census.model._
import spray.json.DefaultJsonProtocol

trait CensusJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val addressInputFormat = jsonFormat4(ParsedInputAddress.apply)
}