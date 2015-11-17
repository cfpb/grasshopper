package grasshopper.test.util

import geometry.Point

import math._

// Calculates the great-circle distance between two points on the surface of the earth (in km)
// See https://en.wikipedia.org/wiki/Haversine_formula
object Haversine {
  val R = 6371 // Mean Earth Radius, see https://en.wikipedia.org/wiki/Earth_radius#Mean_radius

  def distance(p1: Point, p2: Point): Double = {
    val lat1 = p1.y
    val lon1 = p1.x
    val lat2 = p2.y
    val lon2 = p2.x
    val dlat = (lat2 - lat1).toRadians
    val dlon = (lon2 - lon1).toRadians

    val a = pow(sin(dlat / 2), 2) + pow(sin(dlon / 2), 2) * cos(lat1.toRadians) * cos(lat2.toRadians)
    val c = 2 * asin(sqrt(a))
    R * c
  }

}