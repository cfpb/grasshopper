package grasshopper.client.census.model

import feature.Feature

object CensusResult {
  def empty: CensusResult = CensusResult(Nil.toArray)
}

case class CensusResult(features: Array[Feature])
