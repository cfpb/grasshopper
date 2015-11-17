package grasshopper.test.util

import geometry.Point
import org.scalatest.{ MustMatchers, FlatSpec }

class HaversineSpec extends FlatSpec with MustMatchers {

  "The Haversine formula" should "calculate the distance between two coordinates" in {
    val lat1 = 36.12
    val lon1 = -86.67
    val lat2 = 33.94
    val lon2 = -118.40

    val p1 = Point(lon1, lat1)
    val p2 = Point(lon2, lat2)

    Haversine.distance(p1, p2) mustBe 2886.4444428379834
  }
}