name := "computex"

version := "1.0"

scalaVersion :="2.9.2"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8" % "test",
  "commons-configuration" % "commons-configuration" % "1.7",
  "org.apache.jena" % "jena-arq" % "2.9.3",
  "org.rogach" %% "scallop" % "0.9.1" ,
  "com.typesafe" % "config" % "1.0.1"
)

