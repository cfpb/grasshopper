import sbt._

object Dependencies {
  val repos = Seq(
    "Local Maven Repo"  at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo"     at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    Resolver.bintrayRepo("mfglabs", "maven")
  )

  val akkaActor          = "com.typesafe.akka"          %% "akka-actor"                           % Version.akka
  val akkaTestKit        = "com.typesafe.akka"          %% "akka-testkit"                         % Version.akka % "it, test"
  val akkaStreams        = "com.typesafe.akka"          %% "akka-stream-experimental"             % Version.akkaStreams
  val akkaStreamsTestkit = "com.typesafe.akka"          %% "akka-stream-testkit-experimental"     % Version.akkaStreams % "it, test"
  val akkaHttpCore       = "com.typesafe.akka"          %% "akka-http-core-experimental"          % Version.akkaStreams
  val akkaHttp           = "com.typesafe.akka"          %% "akka-http-experimental"               % Version.akkaStreams
  val akkaHttpJson       = "com.typesafe.akka"          %% "akka-http-spray-json-experimental"    % Version.akkaStreams
  val akkaHttpTestkit    = "com.typesafe.akka"          %% "akka-http-testkit-experimental"       % Version.akkaStreams % "it, test"
  val logback            = "ch.qos.logback"              % "logback-classic"                      % Version.logback
  val scalaLogging       = "com.typesafe.scala-logging" %% "scala-logging"                        % Version.scalaLogging
  val config             = "com.typesafe"                % "config"                               % Version.config

  val scalaTest          = "org.scalatest"              %% "scalatest"                            % Version.scalaTest   % "it, test"
  val scalaCheck         = "org.scalacheck"             %% "scalacheck"                           % Version.scalaCheck  % "it, test"

  val es                 = "org.elasticsearch"           % "elasticsearch"                        % Version.elasticsearch
  val mfglabs            = "com.mfglabs"                %% "akka-stream-extensions-elasticsearch" % Version.mfglabs

  val scaleGeoJson       = "com.github.jmarin"          %% "scale-geojson"                        % Version.scale

  val async              = "org.scala-lang.modules"     %% "scala-async"                          % Version.async

  val metricsJvm         = "io.dropwizard.metrics"       % "metrics-jvm"                          % Version.metrics
  val influxDbReporter   = "net.alchim31"                % "metrics-influxdb"                     % Version.influxdbReporter
  val metricsScala       = "nl.grons"                   %% "metrics-scala"                        % Version.metricsScala excludeAll(ExclusionRule(organization = "com.typesafe.akka"))
}
