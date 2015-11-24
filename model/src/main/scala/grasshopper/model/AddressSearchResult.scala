package grasshopper.model

import feature.Feature

object AddressSearchResult {
  def empty: AddressSearchResult = AddressSearchResult(Nil.toArray)
}

case class AddressSearchResult(features: Array[Feature])

