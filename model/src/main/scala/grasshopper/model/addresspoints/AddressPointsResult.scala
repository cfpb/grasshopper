package grasshopper.model.addresspoints

import feature.Feature

object AddressPointsResult {
  def empty: AddressPointsResult = AddressPointsResult("ADDRESS_NOT_FOUND", "", Nil.toArray)
  def error: AddressPointsResult = AddressPointsResult("SERVICE_UNAVAILABLE", "", Nil.toArray)
}

case class AddressPointsResult(status: String, input: String, features: Array[Feature])
