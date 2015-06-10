package grasshopper.geocoder.metrics.schedule

import akka.actor.{ ActorLogging, Actor, Props }
import kamon.Kamon
import kamon.metric.EntitySnapshot

case class SystemMetrics()

object SystemMetricsActor {
  def props: Props = {
    Props(new SystemMetricsActor)
  }
}

class SystemMetricsActor extends Actor with ActorLogging {
  lazy val collectionContext = Kamon.metrics.buildDefaultCollectionContext

  override def receive: Receive = {
    case SystemMetrics =>
      log.debug("Sending Geocoder System Metrics")
      val heapMetrics = takeSnapshotOf("heap-memory", "system-metric")
    case _ => //Do nothing
  }

  private def takeSnapshotOf(name: String, category: String): EntitySnapshot = {
    val recorder = Kamon.metrics.find(name, category).get
    recorder.collect(collectionContext)
  }
}

