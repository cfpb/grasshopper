package grasshopper.feature

import grasshopper.geometry._

object FeatureCollection {
  def apply(features: Feature*): FeatureCollection = {
    FeatureCollection(features.toTraversable)
  }
}

case class FeatureCollection(features: Traversable[Feature]) {

  def count: Int = features.size

  def envelope: Envelope = {
    val envs = features.map(f => f.geometry.envelope)
    envs.reduceLeft[Envelope] { (l, r) =>
      l union r
    }
  }

}
