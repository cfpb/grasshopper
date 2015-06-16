package grasshopper.addresspoints

import grasshopper.addresspoints.api.Service
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.addresspoints.metrics.JvmMetrics
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

import scala.util.Properties

object AddressPointService extends App with Service {
  override implicit val system = ActorSystem("grasshopper-addresspoints")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = Properties.envOrElse("ELASTICSEARCH_HOST", config.getString("grasshopper.addresspoints.elasticsearch.host"))
  lazy val port = Properties.envOrElse("ELASTICSEARCH_PORT", config.getString("grasshopper.addresspoints.elasticsearch.port"))
  lazy val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  val http = Http(system).bindAndHandle(
    routes,
    config.getString("grasshopper.addresspoints.http.interface"),
    config.getInt("grasshopper.addresspoints.http.port")
  )

  lazy val isMonitored = Properties.envOrElse("IS_MONITORED", config.getString("grasshopper.addresspoints.isMonitored")).toBoolean

  if (isMonitored) {
    val jvmMetrics = JvmMetrics
  }

  sys.addShutdownHook {
    system.shutdown()
  }
}
