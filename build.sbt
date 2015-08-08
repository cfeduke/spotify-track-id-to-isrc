name := "Spotify Track ID to ISRC"

organization := "com.deploymentzone"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "io.spray"            %% "spray-json"    % "1.3.1",
  "io.spray"            %% "spray-client"  % "1.3.3",
  "com.typesafe.akka"   %% "akka-actor"    % "2.3.9",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
).map(ld => ld withJavadoc() withSources())

initialCommands := "import com.deploymentzone._"

assemblyJarName in assembly := "deploymentzone-spotify.jar"

