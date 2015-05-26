package grasshopper.client.census.model

import feature.Feature

object CensusResult {
  def empty: CensusResult = CensusResult("ADDRESS_NOT_FOUND", Nil.toArray)
}

case class CensusResult(status: String, features: Array[Feature])
