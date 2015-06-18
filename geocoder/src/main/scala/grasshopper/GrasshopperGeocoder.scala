package grasshopper.geocoder

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import grasshopper.geocoder.metrics.JvmMetrics
import grasshopper.geocoder.api.Service

import scala.util.Properties

object GrasshopperGeocoder extends App with Service {
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

  lazy val isMonitored = Properties.envOrElse("IS_MONITORED", config.getString("grasshopper.geocoder.monitoring.isMonitored")).toBoolean

  if (isMonitored) {
    val jvmMetrics = JvmMetrics
  }

  sys.addShutdownHook {
    system.shutdown()
  }

}
