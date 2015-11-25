package grasshopper.model

import feature.Feature

object AddressSearchResult {
  def empty: AddressSearchResult = AddressSearchResult(List.empty)
}

case class AddressSearchResult(features: List[Feature])
