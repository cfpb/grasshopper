import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import spray.revolver.RevolverPlugin._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "0.0.1"
  val buildScalaVersion = "2.11.5"

  val buildSettings = Defaults.coreDefaultSettings ++
    scalariformSettings ++
    Seq(
      organization  := buildOrganization,
      version       := buildVersion,
      scalaVersion  := buildScalaVersion,
      scalacOptions := Seq("-deprecation", "-unchecked", "-feature")
    )
}

object GrasshopperBuild extends Build {
  import Dependencies._
  import BuildSettings._


  val commonDeps = Seq(scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akkaActor, akkaStreams)

  val jsonDeps = commonDeps ++ Seq(akkaHttpJson, jts)

  val akkaHttpDeps = akkaDeps ++ jsonDeps ++ Seq(akkaHttp, akkaHttpCore, akkaHttpTestkit)

  val esDeps = commonDeps ++ Seq(es)

  val geocodeDeps = akkaHttpDeps ++ esDeps

  
  lazy val grasshopper = Project(
    "grasshopper",
    file("."),
    settings = buildSettings 
  ).aggregate(core, addresspoints)


  lazy val core = Project(
    "core",
    file("core"),
    settings = buildSettings ++ Seq(libraryDependencies ++= jsonDeps)
  )

  lazy val elasticsearch = Project(
    "elasticsearch",
    file("elasticsearch"),
    settings = buildSettings ++ Seq(libraryDependencies ++= esDeps)
  ).dependsOn(core)


  lazy val addresspoints = Project(
    "addresspoints",
    file("addresspoints"),
    settings = buildSettings ++ Revolver.settings ++ Seq(libraryDependencies ++= geocodeDeps)
  ).dependsOn(core, elasticsearch)


}
