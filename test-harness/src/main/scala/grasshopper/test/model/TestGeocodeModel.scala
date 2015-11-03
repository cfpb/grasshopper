package grasshopper.test.model

import geometry.Point

object TestGeocodeModel {
  object PointInputAddress {
    def empty: PointInputAddress = PointInputAddress("", Point(0, 0))
  }

  case class PointInputAddress(inputAddress: String, point: Point)

  object PointInputAddressTract {
    def empty: PointInputAddressTract = PointInputAddressTract(PointInputAddress.empty, "")
  }

  case class PointInputAddressTract(pointInputAddress: PointInputAddress, geoid: String) {
    override def toString: String = s"${pointInputAddress.inputAddress},${pointInputAddress.point.x},${pointInputAddress.point.y},${geoid}"
  }
}
