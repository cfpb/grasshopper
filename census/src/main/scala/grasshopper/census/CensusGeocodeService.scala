package grasshopper.census

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.metrics.JvmMetrics
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import grasshopper.census.api.Service

import scala.util.Properties

object CensusGeocodeService extends App with Service {
  override implicit val system: ActorSystem = ActorSystem("grasshopper-census")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = Properties.envOrElse("ELASTICSEARCH_HOST", config.getString("grasshopper.census.elasticsearch.host"))
  lazy val port = Properties.envOrElse("ELASTICSEARCH_PORT", config.getString("grasshopper.census.elasticsearch.port"))
  lazy val cluster = Properties.envOrElse("ELASTICSEARCH_CLUSTER", config.getString("grasshopper.census.elasticsearch.cluster"))

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
    config.getString("grasshopper.census.http.interface"),
    config.getInt("grasshopper.census.http.port")
  )

  // Default "isMonitored" value set in "metrics" project
  lazy val isMonitored = Properties.envOrElse("IS_MONITORED", config.getString("grasshopper.monitoring.isMonitored")).toBoolean

  if (isMonitored) {
    val jvmMetrics = JvmMetrics
  }

  sys.addShutdownHook {
    client.close()
    system.terminate()
  }

}
