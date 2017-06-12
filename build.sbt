name := "sentinel"

version := "1.0"
scalaVersion := "2.11.11"

organization := "com.inneractive"
isSnapshot := true
resolvers += Resolver.bintrayRepo("hohonuuli", "maven")
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.3",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.ddahl" %% "rscala" % "1.0.13",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.11.51",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.10" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "io.kamon" %% "kamon-datadog" % "0.6.0",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
  "com.github.servicenow.stl4j" % "stl-decomp-4j" % "1.0-SNAPSHOT",
  "scilube" %% "scilube-core" % "2.0",
  "org.apache.commons" % "commons-math3" % "3.2"
).map(_.excludeAll(ExclusionRule("org.slf4j", "slf4j-simple")))




