name := "smpp"

version := "0.1"

scalaVersion := "2.13.1"

Global / cancelable := false

val akkaV = "2.6.3"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.cloudhopper" % "ch-commons-charset" % "3.0.2",
  "com.cloudhopper" % "ch-commons-gsm" % "3.0.0",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.3" % "test"
)

javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation")