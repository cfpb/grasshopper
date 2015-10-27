package grasshopper.addresspoints

import grasshopper.addresspoints.http.HttpService
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.metrics.JvmMetrics
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

object AddressPointService extends App with HttpService {
  override implicit val system = ActorSystem("grasshopper-addresspoints")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = config.getString("grasshopper.addresspoints.elasticsearch.host")
  lazy val port = config.getString("grasshopper.addresspoints.elasticsearch.port")
  lazy val cluster = config.getString("grasshopper.addresspoints.elasticsearch.cluster")

  lazy val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("node.data", false)
    .put("node.master", false)
    .put("cluster.name", cluster)
    .put("client.transport.sniff", true)

  lazy val client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  val http = Http(system).bindAndHandle(
    routes,
    config.getString("grasshopper.addresspoints.http.interface"),
    config.getInt("grasshopper.addresspoints.http.port")
  )

  // Default "isMonitored" value set in "metrics" project
  lazy val isMonitored = config.getString("grasshopper.monitoring.isMonitored").toBoolean

  if (isMonitored) {
    val jvmMetrics = JvmMetrics
  }

  sys.addShutdownHook {
    client.close()
    system.terminate()
  }
}
