import sbtassembly.MergeStrategy

lazy val commonSettings = Seq(
  organization := "de.nn",
  version := "1.0",
  scalaVersion := "2.12.0"
)

name := "HttpScheduler"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies := Seq(
  "io.vertx" % "vertx-web-scala_2.12" % "3.4.1",
  "io.vertx" % "vertx-web-client-scala_2.12" % "3.4.1",
  "io.vertx" %% "vertx-mongo-client-scala" % "3.4.1",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.jayway.jsonpath" % "json-path" % "2.2.0",
  "org.quartz-scheduler" % "quartz" % "2.2.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "net.redhogs.cronparser" % "cron-parser-core" % "2.9"
)

val meta = """META.INF(.)*""".r
val merging : (String) => MergeStrategy = {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}


lazy val schedulerService = (project in file("")).
  settings(commonSettings: _*).
  settings(mainClass in assembly := Some("server.SchedulerVerticle")).
  settings(assemblyMergeStrategy in assembly := merging).
  settings(assemblyJarName := "scheduler_service-prod.jar")

assemblyMergeStrategy in assembly := merging
