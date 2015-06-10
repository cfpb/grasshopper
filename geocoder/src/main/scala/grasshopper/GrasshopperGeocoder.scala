package grasshopper.geocoder

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.geocoder.api.Service
import grasshopper.geocoder.metrics.schedule.{ SystemMetrics, SystemMetricsActor }
import kamon.Kamon
import scala.concurrent.duration._
import scala.language.postfixOps

object GrasshopperGeocoder extends App with Service {
  System.setProperty("java.library.path", "sigar")
  Kamon.start()
  override implicit val system: ActorSystem = ActorSystem("grasshopper-geocoder")

  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  val http = Http(system).bindAndHandle(
    routes,
    config.getString("grasshopper.geocoder.http.interface"),
    config.getInt("grasshopper.geocoder.http.port")
  )

  val systemActor = system.actorOf(SystemMetricsActor.props)
  system.scheduler.schedule(1000 milliseconds, 1000 milliseconds, systemActor, SystemMetrics)

  sys.addShutdownHook {
    system.shutdown()
    Kamon.shutdown()
  }

}
