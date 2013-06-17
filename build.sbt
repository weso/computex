name := "computex"

version := "1.0"

scalaVersion :="2.10.2"

scalacOptions ++= Seq("-deprecation")


libraryDependencies ++= Seq(
  "commons-configuration" % "commons-configuration" % "1.7",
  "org.apache.jena" % "jena-arq" % "2.9.3",
  "org.rogach" %% "scallop" % "0.9.1" ,
  "com.typesafe" % "config" % "1.0.1",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test", 
  "junit" % "junit" % "4.10" % "test"
)

