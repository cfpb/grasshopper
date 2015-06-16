package grasshopper.addresspoints.metrics

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import com.codahale.metrics.MetricFilter
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.typesafe.config.ConfigFactory
import metrics_influxdb.{ InfluxdbReporter, InfluxdbHttp }

import scala.util.Properties

object JvmMetrics extends Instrumented {

  val appName = "addresspoints"
  val hostName = InetAddress.getLocalHost.getHostName

  val config = ConfigFactory.load()

  lazy val influxHost = Properties.envOrElse("INFLUXDB_HOST", config.getString("grasshopper.addresspoints.influxdb.host"))
  lazy val influxPort = Properties.envOrElse("INFLUXDB_PORT", config.getString("grasshopper.addresspoints.influxdb.port")).toInt
  lazy val influxdbUser = Properties.envOrElse("INFLUXDB_USER", config.getString("grasshopper.addresspoints.influxdb.user"))
  lazy val influxdbPassword = Properties.envOrElse("INFLUXDB_PASSWORD", config.getString("grasshopper.addresspoints.influxdb.password"))

  val influxdb = new InfluxdbHttp(influxHost, influxPort, "metrics", influxdbUser, influxdbPassword)

  val reporter = InfluxdbReporter
    .forRegistry(metricRegistry)
    .prefixedWith(appName + "." + hostName)
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .filter(MetricFilter.ALL)
    .skipIdleMetrics(true)
    .build(influxdb)
  reporter.start(10, TimeUnit.SECONDS)

  val memory = new MemoryUsageGaugeSet()
  metricRegistry.register("memory", memory)

}
