package grasshopper.geocoder

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.geocoder.api.stats.GeocodeStatsAggregator
import grasshopper.geocoder.http.HttpService
import grasshopper.geocoder.ws.WebsocketService
import grasshopper.metrics.JvmMetrics
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress

object GrasshopperGeocoder extends App with HttpService with WebsocketService {

  override implicit val system: ActorSystem = ActorSystem("grasshopper-geocoder")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = config.getString("grasshopper.geocoder.elasticsearch.host")
  lazy val port = config.getString("grasshopper.geocoder.elasticsearch.port")
  lazy val cluster = config.getString("grasshopper.geocoder.elasticsearch.cluster")

  lazy val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("node.data", false)
    .put("node.master", false)
    .put("cluster.name", cluster)
    .put("client.transport.sniff", true)

  override lazy val client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  val statsAggregator = system.actorOf(GeocodeStatsAggregator.props, name = "statsAggregator")

  val http = Http(system).bindAndHandle(
    routes ~ wsRoutes,
    config.getString("grasshopper.geocoder.http.interface"),
    config.getInt("grasshopper.geocoder.http.port")
  )

  // Default "isMonitored" value set in "metrics" project
  lazy val isMonitored = config.getString("grasshopper.monitoring.isMonitored").toBoolean

  if (isMonitored) {
    val jvmMetrics = JvmMetrics
  }

  sys.addShutdownHook {
    system.terminate()
  }

}
