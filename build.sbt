
resolvers +=
  "Maven Central" at "http://central.maven.org/maven2/"

lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(gameclient, homepageclient),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val gameclient = (project in file("gameclient")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val homepageclient = (project in file("homepageclient")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
  organization := "brt",
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core" % "0.9.3",
    "io.circe" %%% "circe-generic" % "0.9.3",
    "io.circe" %%% "circe-parser" % "0.9.3"
    )
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}
