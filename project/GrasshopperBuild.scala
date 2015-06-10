import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import spray.revolver.RevolverPlugin._
import wartremover._
import sbtassembly.AssemblyPlugin.autoImport._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "0.0.1"
  val buildScalaVersion = "2.11.6"

  val buildSettings = Defaults.coreDefaultSettings ++
    scalariformSettings ++
    wartremoverSettings ++
    Defaults.itSettings ++
    Seq(
      organization  := buildOrganization,
      version       := buildVersion,
      scalaVersion  := buildScalaVersion,
      //wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad, Wart.NonUnitStatements),
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature")
    )
}

object GrasshopperBuild extends Build {
  import Dependencies._
  import BuildSettings._

  val commonDeps = Seq(logback, scalaLogging, logstashLogback, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akkaActor, akkaStreams)

  val jsonDeps = commonDeps ++ Seq(akkaHttpJson)

  val akkaHttpDeps = akkaDeps ++ jsonDeps ++ Seq(akkaHttp, akkaHttpCore, akkaHttpTestkit)

  val esDeps = commonDeps ++ Seq(es, scaleGeoJson)

  val scaleDeps = Seq(scaleGeoJson)

  val geocodeDeps = akkaHttpDeps ++ esDeps ++ scaleDeps

  val asyncDeps = Seq(async)

  val kamonDeps = Seq(kamonCore, kamonStatsd, kamonMetrics)

    
  lazy val grasshopper = (project in file("."))
    .settings(buildSettings: _*)
    .aggregate(geocoder, addresspoints, census, client)


  lazy val elasticsearch = (project in file("elasticsearch"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= esDeps,
        resolvers ++= repos
      )
    )

  lazy val addresspoints = (project in file("addresspoints"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        libraryDependencies ++= geocodeDeps ++ kamonDeps,
        resolvers ++= repos
      )
    ).dependsOn(elasticsearch)

  lazy val census = (project in file("census"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++ 
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        libraryDependencies ++= geocodeDeps ++ kamonDeps,
        resolvers ++= repos
      )
    ).dependsOn(elasticsearch)

  lazy val client = (project in file("client"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++
        Seq(
          assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
          libraryDependencies ++= akkaHttpDeps ++ scaleDeps ++ asyncDeps ++ kamonDeps
        )
    )

  lazy val geocoder = (project in file("geocoder"))
    .configs( IntegrationTest )
    .settings(buildSettings: _*)
    .settings(
      Revolver.settings ++
      Seq(
        assemblyJarName in assembly := {s"grasshopper-${name.value}.jar"},
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= akkaHttpDeps ++ scaleDeps ++ asyncDeps ++ kamonDeps,
        resolvers ++= repos
      )
    ).dependsOn(client)




}
