package grasshopper.client.addresspoints.model

import feature.Feature

object AddressPointsResult {
  def empty: AddressPointsResult = AddressPointsResult("ADDRESS_NOT_FOUND", Nil.toArray)
  def error: AddressPointsResult = AddressPointsResult("SERVICE_UNAVAILABLE", Nil.toArray)
}

case class AddressPointsResult(status: String, features: Array[Feature])
