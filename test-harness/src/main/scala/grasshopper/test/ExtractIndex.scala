package grasshopper.test

import java.io.File
import java.net.InetAddress
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Source, Flow, Sink }
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.util.ByteString
import com.mfglabs.stream.ExecutionContextForBlockingOps
import com.mfglabs.stream.extensions.elasticsearch.EsStream
import com.typesafe.config.ConfigFactory
import feature.Feature
import grasshopper.test.model.TestGeocodeModel.{ PointInputAddressTract, PointInputAddress }
import grasshopper.test.streams.FlowUtils
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.shield.ShieldPlugin
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import spray.json._
import io.geojson.FeatureJsonProtocol._
import hmda.geo.client.api.HMDAGeoClient
import hmda.geo.client.api.model.census.HMDAGeoTractResult

object ExtractIndex extends FlowUtils {

  implicit val system = ActorSystem("grasshopper-test-harness-extractIndex")
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

  def extractIndexToFile(index: String, indexType: String): Unit = {

    val source = addressPointsStream(index, indexType)

    source
      .via(jsonToPointInputAddress)
      .via(tractOverlay)
      .map(c => c.toCSV + "\n")
      .map(ByteString(_))
      .runWith(FileIO.toFile(new File(s"test-harness/target/${index}-${indexType}.csv")))
      .onComplete {
        case _ =>
          println("Extracted file - DONE")
          client.close()
          system.terminate()
      }
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Index and type required")
      sys.exit(0)
    } else {
      val index = args(0)
      val indexType = args(1)
      extractIndexToFile(index, indexType)
    }

  }

  def addressPointsStream(index: String, indexType: String)(implicit client: Client): Source[String, Unit] = {
    implicit val ec = ExecutionContextForBlockingOps(ExecutionContext.Implicits.global)
    EsStream
      .queryAsStream(
        QueryBuilders.matchAllQuery(),
        index = index,
        `type` = indexType,
        scrollKeepAlive = 1.minutes,
        scrollSize = 10
      )
  }

  def tractOverlay(implicit ec: ExecutionContext): Flow[PointInputAddress, PointInputAddressTract, NotUsed] = {
    Flow[PointInputAddress]
      .mapAsyncUnordered(numProcessors) { i =>
        val p = i.point
        for {
          x <- HMDAGeoClient.findTractByPoint(p) if x.isRight
          y = x.right.getOrElse(HMDAGeoTractResult.empty)
          geoid = y.geoid
        } yield PointInputAddressTract(i, geoid)
      }
      .withAttributes(supervisionStrategy(resumingDecider))
  }

  def jsonToPointInputAddress: Flow[String, PointInputAddress, NotUsed] = {
    Flow[String]
      .map { s =>
        val f = s.parseJson.convertTo[Feature]
        val p = f.geometry.centroid
        val a = f.get("address").getOrElse("").toString
        PointInputAddress(a, p)
      }
  }
}
