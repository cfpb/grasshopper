package grasshopper.metrics

import com.codahale.metrics.MetricRegistry

object Registry {
  val metricsRegistry = new MetricRegistry
}
