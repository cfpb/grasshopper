import sbt._
import sbt.Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.AssemblyPlugin.autoImport._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.8"

  val buildSettings = Defaults.coreDefaultSettings ++
    Defaults.itSettings ++
    Seq(
      organization  := buildOrganization,
      version       := buildVersion,
      scalaVersion  := buildScalaVersion,
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature"),
      aggregate in assembly := false
    )
}

object GrasshopperBuild extends Build {
  import Dependencies._
  import BuildSettings._

  val commonDeps = Seq(logback, scalaLogging, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akkaActor, akkaStreams, akkaTestKit, akkaStreamsTestkit)

  val jsonDeps = commonDeps ++ Seq(akkaHttpJson)

  val akkaHttpDeps = akkaDeps ++ jsonDeps ++ Seq(akkaHttp, akkaHttpCore, akkaHttpTestkit)

  val esDeps = commonDeps ++ Seq(es, esShield, scaleGeoJson)

  val scaleDeps = Seq(scaleGeoJson)

  val metricsDeps = Seq(config, metricsScala, metricsJvm, influxDbReporter)

  val geocodeDeps = akkaHttpDeps ++ esDeps ++ scaleDeps ++ metricsDeps

  val asyncDeps = Seq(async)

  val mfgLabs = Seq(mfglabs)

    
  lazy val grasshopper = (project in file("."))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"${name.value}.jar"},
        mainClass in assembly := Some("grasshopper.geocoder.GrasshopperGeocoder"),
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          // Elasticsearch has its own unshaded org.joda.time.base.BaseDateTime
          // https://www.elastic.co/blog/to-shade-or-not-to-shade
          case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        resolvers ++= repos
      )
    )
    .dependsOn(geocoder)
    .aggregate(client, metrics, model, geocoder)


  lazy val elasticsearch = (project in file("elasticsearch"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= esDeps,
        resolvers ++= repos
      )
    )

  lazy val metrics = (project in file("metrics"))
    .configs(IntegrationTest)
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        libraryDependencies ++= metricsDeps,
        resolvers ++= repos
      )
    )


  lazy val client = (project in file("client"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
        Seq(
          assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
          libraryDependencies ++= akkaHttpDeps ++ scaleDeps ++ asyncDeps
        )
    ).dependsOn(model)

  lazy val geocoder = (project in file("geocoder"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        libraryDependencies ++= geocodeDeps,
        resolvers ++= repos
      )
    ).dependsOn(client, metrics, elasticsearch)


  lazy val model = (project in file("model"))
    .configs(IntegrationTest)
    .settings(buildSettings: _*)
    .settings(
      Seq(
         assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
         assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= jsonDeps ++ scaleDeps
      )
    )

  // FIXME: A better solution would be to push hmda-geo to a Maven repo and import
  //        it as a standard dependency.
  lazy val hmdaGeo = ProjectRef(uri("git://github.com/cfpb/hmda-geo.git"), "client")

  // NOTE: Use this method when referencing a locally modified version of hmda-geo 
  //lazy val hmdaGeo = ProjectRef(file("../hmda-geo"), "client")

  lazy val test_harness = (project in file("test-harness"))
    .configs(IntegrationTest)
    .settings(buildSettings: _*)
    .settings(mainClass in assembly := Some("grasshopper.test.GeocoderTest"))
    .settings(
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case "logback.xml" => MergeStrategy.last
          // Elasticsearch has its own unshaded org.joda.time.base.BaseDateTime
          // https://www.elastic.co/blog/to-shade-or-not-to-shade
          case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= akkaHttpDeps ++ scaleDeps ++ mfgLabs,
        resolvers ++= repos
      )
    )
    .dependsOn(geocoder, hmdaGeo)
    .aggregate(client, hmdaGeo, model, geocoder)

}
