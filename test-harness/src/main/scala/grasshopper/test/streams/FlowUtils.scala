package grasshopper.test.streams

import akka.NotUsed
import akka.stream.ActorAttributes._
import akka.stream.Supervision._
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import feature.Feature
import grasshopper.test.model.TestGeocodeModel.{ PointInputAddress, PointInputAddressTract }

trait FlowUtils {

  val numProcessors = Runtime.getRuntime.availableProcessors()

  def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)

  def string2ByteStringFlow: Flow[String, ByteString, NotUsed] =
    Flow[String].map(str => ByteString(str))

}
