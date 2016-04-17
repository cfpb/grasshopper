package grasshopper.geocoder

import java.net.InetAddress
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
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.shield.ShieldPlugin

object GrasshopperGeocoder extends App with HttpService with WebsocketService {

  override implicit val system: ActorSystem = ActorSystem("grasshopper-geocoder")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = config.getString("grasshopper.geocoder.elasticsearch.host")
  lazy val port = config.getString("grasshopper.geocoder.elasticsearch.port")
  lazy val cluster = config.getString("grasshopper.geocoder.elasticsearch.cluster")

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

  override lazy val client = clientBuilder
    .settings(settings)
    .build()
    .addTransportAddress(new InetSocketTransportAddress(
      InetAddress.getByName(host),
      port.toInt
    ))

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
