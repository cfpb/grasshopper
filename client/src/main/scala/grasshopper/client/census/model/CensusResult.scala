package grasshopper.client.census.model

import feature.Feature

object CensusResult {
  def empty: CensusResult = CensusResult("ADDRESS_NOT_FOUND", Nil.toArray)
  def error: CensusResult = CensusResult("SERVICE_UNAVAILABLE", Nil.toArray)
}

case class CensusResult(status: String, features: Array[Feature])
