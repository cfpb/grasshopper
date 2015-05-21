package grasshopper.client.addresspoints.model

import feature.Feature

object AddressPointsResult {
  def empty: AddressPointsResult = AddressPointsResult(Nil.toArray)
}

case class AddressPointsResult(features: Array[Feature])
