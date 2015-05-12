import sbt._

object Dependencies {
  val repos = Seq(
    "Local Maven Repo"  at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo"     at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  )

  val akkaActor       = "com.typesafe.akka"          %% "akka-actor"                           % Version.akka
  val akkaStreams     = "com.typesafe.akka"          %% "akka-stream-experimental"             % Version.akkaStreams
  val akkaHttpCore    = "com.typesafe.akka"          %% "akka-http-core-experimental"          % Version.akkaStreams
  val akkaHttp        = "com.typesafe.akka"          %% "akka-http-scala-experimental"         % Version.akkaStreams
  val akkaHttpJson    = "com.typesafe.akka"          %% "akka-http-spray-json-experimental"    % Version.akkaStreams
  val akkaHttpTestkit = "com.typesafe.akka"          %% "akka-http-testkit-scala-experimental" % Version.akkaStreams % "test"

  val logback         = "ch.qos.logback"              % "logback-classic"                      % Version.logback
  val scalaLogging    = "com.typesafe.scala-logging" %% "scala-logging"                        % Version.scalaLogging
  val logstashLogback = "net.logstash.logback"        % "logstash-logback-encoder"             % Version.logstashLogback

  val scalaTest       = "org.scalatest"              %% "scalatest"                            % Version.scalaTest   % "test"
  val scalaCheck      = "org.scalacheck"             %% "scalacheck"                           % Version.scalaCheck  % "test"

  val es              = "org.elasticsearch"           % "elasticsearch"                        % Version.elasticsearch

  val scaleGeoJson    = "com.github.jmarin"          %% "scale-geojson"                        % Version.scale

  val async           = "org.scala-lang.modules"     %% "scala-async"                          % Version.async

}
