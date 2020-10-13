name := "file-retention"
version := "0.0.1"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Maven central" at "https://central.maven.org/maven2/"
)

val CatsVersion   = "2.1.1"

libraryDependencies ++= Seq(
  "org.postgresql"        % "postgresql"           % "42.2.16",
  "org.typelevel"         %% "cats-effect"         % CatsVersion,
  "com.github.pureconfig" %% "pureconfig"          % "0.12.2",
  "com.google.cloud"      % "google-cloud-storage" % "1.113.0",
  "com.typesafe.akka"     %% "akka-actor"           % "2.6.10"
)