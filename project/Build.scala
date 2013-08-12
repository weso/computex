import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "Computex"
  val appVersion      = "0.0.1-SNAPSHOT"
  val scalaVersion = "2.10.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "commons-configuration" % "commons-configuration" % "1.7",
    "org.apache.jena" % "jena-arq" % "2.10.1",
    "commons-io" % "commons-io" % "2.4",
    "org.rogach" %% "scallop" % "0.9.1" ,
    "com.typesafe" % "config" % "1.0.1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
