package grasshopper.test

import java.io.File

import akka.actor.ActorSystem
import akka.stream.{ ActorAttributes, FlowShape, ActorMaterializer }
import akka.stream.io.Framing
import akka.stream.scaladsl._
import akka.stream.Supervision._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import feature.Feature
import geometry.Point
import grasshopper.geocoder.api.geocode.GeocodeFlow
import grasshopper.geocoder.model.GeocodeResponse
import grasshopper.test.model.TestGeocodeModel._
import grasshopper.test.streams.FlowUtils
import hmda.geo.client.api.HMDAGeoClient
import hmda.geo.client.api.model.census.HMDAGeoTractResult
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress

import scala.concurrent.ExecutionContext

object GeocoderTest extends GeocodeFlow with FlowUtils {

  implicit val system = ActorSystem("grasshopper-test-harness-census")
  implicit val mat = ActorMaterializer()(system)
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  lazy val host = config.getString("grasshopper.test-harness.elasticsearch.host")
  lazy val port = config.getString("grasshopper.test-harness.elasticsearch.port")
  lazy val cluster = config.getString("grasshopper.test-harness.elasticsearch.cluster")

  lazy val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("node.data", false)
    .put("node.master", false)
    .put("cluster.name", cluster)
    .put("client.transport.sniff", true)

  implicit lazy val client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  def main(args: Array[String]): Unit = {
    println("Testing Geocoder")

    if (args.length < 2) {
      println("Please provide input file to process and output results file")
      sys.exit(1)
    }

    val f = new File(args(0))
    val outputFile = new File(args(1))

    val source = FileIO.fromFile(f)

    source
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      .via(byte2StringFlow)
      .via(stringToPointInputAddressTract)
      .via(geocodeTestFlow.withAttributes(ActorAttributes.supervisionStrategy(resumingDecider)))
      .via(resultsToCSV)
      .via(string2ByteStringFlow)
      .runWith(FileIO.toFile(outputFile))
      .onComplete {
        case _ =>
          println("DONE!")
          client.close()
          system.terminate()
      }

  }

  private def stringToPointInputAddressTract: Flow[String, PointInputAddressTract, Unit] = {
    Flow[String]
      .map { s =>
        val parts = s.split(",")
        val x = parts(0).toDouble
        val y = parts(1).toDouble
        val address = parts(2)
        val geoid = parts(3)
        val point = Point(x, y)
        val pointInputAddress = PointInputAddress(address, point)
        PointInputAddressTract(pointInputAddress, geoid)
      }
  }

  private def geocodeTestFlow(implicit ec: ExecutionContext): Flow[PointInputAddressTract, GeocodeTestResult, Unit] = {
    Flow.fromGraph(
      GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val input = b.add(Flow[PointInputAddressTract])
        val inputBcast = b.add(Broadcast[PointInputAddressTract](2))
        val address = b.add(extractInputAddress)
        val geocode = b.add(geocodeFlow)
        val geocodeResult = b.add(geocodeResponseToGeocodeResult)
        val geocodeResultTract = b.add(geocodeResultToGeocodeResultTract)
        val zip = b.add(Zip[PointInputAddressTract, GeocodeResultTract])
        val output = b.add(flattenResults)

        input ~> inputBcast
        inputBcast ~> address ~> geocode ~> geocodeResult ~> geocodeResultTract ~> zip.in1
        inputBcast ~> zip.in0
        zip.out ~> output

        FlowShape(input.in, output.outlet)
      }
    )
  }

  private def extractInputAddress: Flow[PointInputAddressTract, String, Unit] = {
    Flow[PointInputAddressTract].map(i => i.pointInputAddress.inputAddress)
  }

  private def geocodeResponseToGeocodeResult: Flow[GeocodeResponse, GeocodeResult, Unit] = {
    Flow[GeocodeResponse]
      .map { r =>
        val features = r.features
        val pointFeatureOption = features.find(f => f.get("source").getOrElse("") == "state-address-points")
        val pointFeature = pointFeatureOption.getOrElse(Feature(Point(0, 0)))
        val pointAddress = pointFeature.get("address").getOrElse("").toString
        val censusFeatureOption = features.find(f => f.get("source").getOrElse("") == "census-tiger")
        val censusFeature = censusFeatureOption.getOrElse(Feature(Point(0, 0)))
        val censusAddress = censusFeature.get("address").getOrElse("").toString
        val pointInputAddress = PointInputAddress(pointAddress, pointFeature.geometry.centroid)
        val censusInputAddress = PointInputAddress(censusAddress, censusFeature.geometry.centroid)
        GeocodeResult(pointInputAddress, censusInputAddress)
      }
  }

  private def geocodeResultToGeocodeResultTract: Flow[GeocodeResult, GeocodeResultTract, Unit] = {
    Flow[GeocodeResult]
      .mapAsync(numProcessors) { r =>
        val pointInput = r.pointResult
        val censusInput = r.censusResult
        val p = pointInput.point
        val c = censusInput.point
        for {
          xs <- HMDAGeoClient.findTractByPoint(p) if xs.isRight
          x = xs.right.getOrElse(HMDAGeoTractResult.empty)
          ys <- HMDAGeoClient.findTractByPoint(c) if ys.isRight
          y = ys.right.getOrElse(HMDAGeoTractResult.empty)
          xGeoID = x.geoid
          yGeoID = y.geoid
          pointInputAddressTract = PointInputAddressTract(pointInput, xGeoID)
          censusInputAddressTract = PointInputAddressTract(censusInput, yGeoID)
        } yield GeocodeResultTract(pointInputAddressTract, censusInputAddressTract)
      }.withAttributes(ActorAttributes.supervisionStrategy(resumingDecider))
  }

  private def flattenResults: Flow[(PointInputAddressTract, GeocodeResultTract), GeocodeTestResult, Unit] = {
    Flow[(PointInputAddressTract, GeocodeResultTract)]
      .map { x =>
        val p = x._1
        val g = x._2
        val pr = g.pointResultTract
        val cr = g.censusResultTract
        GeocodeTestResult(p, pr, cr)
      }
  }

  private def resultsToCSV: Flow[GeocodeTestResult, String, Unit] = {
    Flow[GeocodeTestResult].map(g => g.toCSV)
  }

}
