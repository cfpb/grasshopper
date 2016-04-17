package grasshopper.test

import java.io.File
import java.net.InetAddress
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ FlowShape, Supervision, ActorMaterializer }
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl._
import akka.stream.ActorAttributes.supervisionStrategy
import Supervision.resumingDecider
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import feature.Feature
import geometry.Point
import grasshopper.geocoder.api.geocode.GeocodeFlow
import grasshopper.test.model.TestGeocodeModel.{ CensusOverlayResult, PointInputAddress, PointInputAddressTract }
import grasshopper.test.streams.FlowUtils
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.shield.ShieldPlugin
import hmda.geo.client.api.HMDAGeoClient
import hmda.geo.client.api.model.census.HMDAGeoTractResult

object TractOverlay extends GeocodeFlow with FlowUtils {

  implicit val system = ActorSystem("grasshopper-test-harness-census")
  implicit val mat = ActorMaterializer()(system)
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  lazy val host = config.getString("grasshopper.test-harness.elasticsearch.host")
  lazy val port = config.getString("grasshopper.test-harness.elasticsearch.port")
  lazy val cluster = config.getString("grasshopper.test-harness.elasticsearch.cluster")

  lazy val settings = Settings.settingsBuilder()
    .put("http.enabled", false)
    .put("node.data", false)
    .put("node.master", false)
    .put("cluster.name", cluster)
    .put("client.transport.sniff", true)

  lazy val clientBuilder = TransportClient.builder()

  lazy val user = config.getString("grasshopper.geocoder.elasticsearch.user")
  lazy val password = config.getString("grasshopper.geocoder.elasticsearch.password")

  if (user.nonEmpty && password.nonEmpty) {
    settings.put("shield.user", String.format("%s:%s", user, password))
    clientBuilder.addPlugin(classOf[ShieldPlugin])
  }

  implicit lazy val client = clientBuilder
    .settings(settings)
    .build()
    .addTransportAddress(new InetSocketTransportAddress(
      InetAddress.getByName(host),
      port.toInt
    ))

  def main(args: Array[String]): Unit = {
    println("Processing Address Points")

    if (args.length < 1) {
      println("Please provide a file to process")
      sys.exit(1)
    }

    val f = new File(args(0))

    val source = FileIO.fromFile(f)

    source
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      .via(byte2StringFlow)
      .via(str2PointInputAddressFlow)
      .via(censusOverlayFlow.map(_.toCSV))
      .via(string2ByteStringFlow)
      .runWith(FileIO.toFile(new File("test-harness/target/census-results.csv")))
      .onComplete {
        case _ =>
          println("*** DONE!! ***")
          client.close()
          system.terminate()
      }

  }

  def str2PointInputAddressFlow: Flow[String, PointInputAddressTract, NotUsed] = {
    Flow[String].map { str =>
      val parts = str.split(",")
      if (parts.length == 4) {
        val address = parts(0)
        val longitude = parts(1).toDouble
        val latitude = parts(2).toDouble
        val geoid = parts(3)
        val point = Point(longitude, latitude)
        val pointInput = PointInputAddress(address, point)
        PointInputAddressTract(pointInput, geoid)
      } else {
        PointInputAddressTract.empty
      }
    }
  }

  def pointInput2CensusGeocodeFlow: Flow[PointInputAddressTract, Feature, NotUsed] = {
    Flow[PointInputAddressTract]
      .map(p => p.pointInputAddress.inputAddress)
      .via(parseFlow.withAttributes(supervisionStrategy(resumingDecider)))
      .via(parsedInputAddressFlow)
      .via(geocodeLineFlow.withAttributes(supervisionStrategy(resumingDecider)))
  }

  def outputCensusTractFlow: Flow[Feature, PointInputAddressTract, NotUsed] = {
    Flow[Feature]
      .mapAsync(numProcessors) { f =>
        val p = f.geometry.centroid
        val i = PointInputAddress("", p)
        for {
          x <- HMDAGeoClient.findTractByPoint(p) if x.isRight
          y = x.right.getOrElse(HMDAGeoTractResult.empty)
          geoid = y.geoid
        } yield PointInputAddressTract(i, geoid.toString)
      }.withAttributes(supervisionStrategy(resumingDecider))
  }

  def pointList2CensusOverlayFlow: Flow[List[PointInputAddressTract], CensusOverlayResult, NotUsed] = {
    Flow[List[PointInputAddressTract]]
      .map(xs => CensusOverlayResult(xs.head, xs.tail.head))
  }

  def censusOverlayFlow: Flow[PointInputAddressTract, CensusOverlayResult, NotUsed] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val input = b.add(Flow[PointInputAddressTract])
        val broadcast = b.add(Broadcast[PointInputAddressTract](2))
        val geocode = b.add(pointInput2CensusGeocodeFlow)
        val outputTract = b.add(outputCensusTractFlow)
        val zip = b.add(Zip[PointInputAddressTract, PointInputAddressTract])
        val censusOverlay = b.add(tupleToListFlow[PointInputAddressTract].via(pointList2CensusOverlayFlow))

        input ~> broadcast ~> zip.in0
        broadcast ~> geocode ~> outputTract ~> zip.in1
        zip.out ~> censusOverlay

        FlowShape(input.in, censusOverlay.outlet)
      }
    )
  }

}
