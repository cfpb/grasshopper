package grasshopper.addresspoints.metrics

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.MetricFilter
import com.codahale.metrics.jvm.{ GarbageCollectorMetricSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet }
import com.typesafe.config.ConfigFactory
import metrics_influxdb.{ InfluxdbHttp, InfluxdbReporter }

import scala.util.Properties

object JvmMetrics extends Instrumented {

  val appName = "addresspoints"
  val hostName = InetAddress.getLocalHost.getHostName

  val config = ConfigFactory.load()

  lazy val influxHost = Properties.envOrElse("INFLUXDB_HOST", config.getString("grasshopper.addresspoints.monitoring.influxdb.host"))
  lazy val influxPort = Properties.envOrElse("INFLUXDB_PORT", config.getString("grasshopper.addresspoints.monitoring.influxdb.port")).toInt
  lazy val influxdbUser = Properties.envOrElse("INFLUXDB_USER", "")
  lazy val influxdbPassword = Properties.envOrElse("INFLUXDB_PASSWORD", "")
  lazy val monitoringFrequency = Properties.envOrElse("MONITORING_FREQUENCY", config.getString("grasshopper.addresspoints.monitoring.frequency")).toInt

  val influxdb = new InfluxdbHttp(influxHost, influxPort, "metrics", influxdbUser, influxdbPassword)

  val reporter = InfluxdbReporter
    .forRegistry(metricRegistry)
    .prefixedWith(appName + "." + hostName)
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .filter(MetricFilter.ALL)
    .skipIdleMetrics(true)
    .build(influxdb)
  reporter.start(monitoringFrequency, TimeUnit.SECONDS)

  val memory = new MemoryUsageGaugeSet()
  metricRegistry.register("memory", memory)
  val gc = new GarbageCollectorMetricSet()
  metricRegistry.register("gc", gc)
  val threads = new ThreadStatesGaugeSet()
  metricRegistry.register("threads", threads)

}
