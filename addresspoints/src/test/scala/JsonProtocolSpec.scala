package grasshopper.addresspoints

import org.scalatest._
import spray.json._
import java.util.Calendar

class JsonProtocolSpec extends FlatSpec with MustMatchers with JsonProtocol {

  "Status" should "convert to and from JSON" in {
    val date = Calendar.getInstance().getTime().toString
    val status = Status("OK", date)
    println(status.toJson)
    status.toJson.convertTo[Status] mustBe status
  }

}

