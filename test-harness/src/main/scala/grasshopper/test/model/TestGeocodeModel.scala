package grasshopper.test.model

import geometry.Point
import grasshopper.test.util.Haversine

object TestGeocodeModel {
  object PointInputAddress {
    def empty: PointInputAddress = PointInputAddress("", Point(0, 0))
  }

  case class PointInputAddress(inputAddress: String, point: Point)

  object PointInputAddressTract {
    def empty: PointInputAddressTract = PointInputAddressTract(PointInputAddress.empty, "")
  }

  case class PointInputAddressTract(pointInputAddress: PointInputAddress, geoid: String) {
    override def toString = toCSV
    def toCSV: String = s"${pointInputAddress.inputAddress},${pointInputAddress.point.x},${pointInputAddress.point.y},${geoid}"
  }

  case class PointOverlayResult(inputPoint: PointInputAddressTract, outputPoint: PointInputAddressTract) {
    def toCSV: String = s"${inputPoint.pointInputAddress.inputAddress}," +
      s"${inputPoint.pointInputAddress.point.x}," +
      s"${inputPoint.pointInputAddress.point.y}," +
      s"${inputPoint.geoid}," +
      s"${outputPoint.pointInputAddress.point.x}," +
      s"${outputPoint.pointInputAddress.point.y}," +
      s"${outputPoint.geoid}\n"

  }

  case class CensusOverlayResult(inputPoint: PointInputAddressTract, outputPoint: PointInputAddressTract) {
    def dist: Double = Haversine.distance(this.inputPoint.pointInputAddress.point, this.outputPoint.pointInputAddress.point)

    def toCSV: String = s"${inputPoint.pointInputAddress.inputAddress}," +
      s"${inputPoint.pointInputAddress.point.x}," +
      s"${inputPoint.pointInputAddress.point.y}," +
      s"${inputPoint.geoid}," +
      s"${outputPoint.pointInputAddress.point.x}," +
      s"${outputPoint.pointInputAddress.point.y}," +
      s"${dist}," +
      s"${outputPoint.geoid}\n"

  }

  case class GeocodeResult(pointResult: PointInputAddress, censusResult: PointInputAddress)

  case class GeocodeResultTract(pointResultTract: PointInputAddressTract, censusResultTract: PointInputAddressTract)

  case class GeocodeTestResult(inputPoint: PointInputAddressTract, pointResult: PointInputAddressTract, censusResult: PointInputAddressTract) {
    def toCSV: String = {
      s"${inputPoint.pointInputAddress.inputAddress}," +
        s"${inputPoint.pointInputAddress.point.x}," +
        s"${inputPoint.pointInputAddress.point.y}," +
        s"${inputPoint.geoid}," +
        s"${pointResult.pointInputAddress.inputAddress}," +
        s"${pointResult.pointInputAddress.point.x}," +
        s"${pointResult.pointInputAddress.point.y}," +
        s"${pointResult.geoid}," +
        s"${Haversine.distance(inputPoint.pointInputAddress.point, pointResult.pointInputAddress.point)}," +
        s"${censusResult.pointInputAddress.inputAddress}," +
        s"${censusResult.pointInputAddress.point.x}," +
        s"${censusResult.pointInputAddress.point.y}," +
        s"${censusResult.geoid}," +
        s"${Haversine.distance(inputPoint.pointInputAddress.point, censusResult.pointInputAddress.point)}\n"

    }
  }

}
