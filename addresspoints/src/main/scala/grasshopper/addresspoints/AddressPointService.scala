package grasshopper.addresspoints

import grasshopper.addresspoints.api.Service
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.addresspoints.metrics.schedule.{ SystemMetrics, SystemMetricsActor }
import kamon.Kamon
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import scala.util.Properties
import scala.concurrent.duration._
import scala.language.postfixOps

object AddressPointService extends App with Service {
  System.setProperty("java.library.path", "sigar")
  Kamon.start()
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

  val systemActor = system.actorOf(SystemMetricsActor.props)
  system.scheduler.schedule(1000 milliseconds, 1000 milliseconds, systemActor, SystemMetrics)

  sys.addShutdownHook {
    system.shutdown()
    Kamon.shutdown()
  }

}
