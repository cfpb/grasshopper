package grasshopper.census

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.census.metrics.schedule.{ SystemMetrics, SystemMetricsActor }
import kamon.Kamon
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import grasshopper.census.api.Service
import scala.util.Properties
import scala.concurrent.duration._
import scala.language.postfixOps

object CensusGeocodeService extends App with Service {
  System.setProperty("java.library.path", "sigar")
  Kamon.start()
  override implicit val system: ActorSystem = ActorSystem("grasshopper-census")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  lazy val host = Properties.envOrElse("ELASTICSEARCH_HOST", config.getString("grasshopper.census.elasticsearch.host"))
  lazy val port = Properties.envOrElse("ELASTICSEARCH_PORT", config.getString("grasshopper.census.elasticsearch.port"))
  lazy val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port.toInt))

  val http = Http(system).bindAndHandle(
    routes,
    config.getString("grasshopper.census.http.interface"),
    config.getInt("grasshopper.census.http.port")
  )

  val systemActor = system.actorOf(SystemMetricsActor.props)
  system.scheduler.schedule(1000 milliseconds, 1000 milliseconds, systemActor, SystemMetrics)

  sys.addShutdownHook {
    system.shutdown()
    Kamon.shutdown()
  }

}