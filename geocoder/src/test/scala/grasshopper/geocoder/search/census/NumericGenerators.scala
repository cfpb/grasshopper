package grasshopper.geocoder.search.census

import org.scalacheck.Gen

trait NumericGenerators {

  def digits: Gen[Int] = {
    for {
      n <- Gen.choose(-10000, 10000)
    } yield n
  }

  def positive: Gen[Int] = {
    for {
      n <- Gen.choose(0, 10000)
    } yield n
  }

}