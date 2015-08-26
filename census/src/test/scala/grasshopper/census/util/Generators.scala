package grasshopper.census.util

import org.scalacheck.{ Arbitrary, Gen }

trait Generators {

  case class Alphanumeric(s: String) {
    override def toString = s.toString
  }

  val unicodeChar = Gen.choose(Char.MinValue, Char.MaxValue).filter(c => !Character.isDigit(c))

  val alphanumericGen: Gen[Alphanumeric] = {
    for {
      x <- Gen.oneOf("A", "B", "C")
      y <- Gen.choose(Int.MinValue, Int.MaxValue)
    } yield Alphanumeric(List(x, y).mkString)
  }

  implicit def arbAlphanumeric: Arbitrary[Alphanumeric] = Arbitrary(alphanumericGen)

}
