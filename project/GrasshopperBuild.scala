import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import spray.revolver.RevolverPlugin._
import wartremover._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "0.0.1"
  val buildScalaVersion = "2.11.6"

  val buildSettings = Defaults.coreDefaultSettings ++
    scalariformSettings ++
    wartremoverSettings ++
    Seq(
      organization  := buildOrganization,
      version       := buildVersion,
      scalaVersion  := buildScalaVersion,
      wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad, Wart.NonUnitStatements),
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature",
        "-Xfatal-warnings")
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

  
  lazy val grasshopper = Project(
    "grasshopper",
    file("."),
    settings = buildSettings 
  ).aggregate(addresspoints)

  lazy val elasticsearch = Project(
    "elasticsearch",
    file("elasticsearch"),
    settings = buildSettings ++ Seq(libraryDependencies ++= esDeps, resolvers ++= repos)
  )


  lazy val addresspoints = Project(
    "addresspoints",
    file("addresspoints"),
    settings = buildSettings ++ Revolver.settings ++ Seq(libraryDependencies ++= geocodeDeps, resolvers ++= repos)
  ).dependsOn(elasticsearch)


}
