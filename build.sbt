name := "file-retention"
version := "0.0.1"

scalaVersion := "2.13.4"

resolvers ++= Seq(
  "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
  "Maven central" at "https://central.maven.org/maven2/"
)

val catsEffectsVersion   = "2.3.1"
val doobieVersion = "0.9.2"
val Log4CatsVersion = "1.1.1"
val Slf4jVersion = "1.7.30"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.3.0",
  "org.typelevel" %% "cats-effect" % catsEffectsVersion,
  "com.github.pureconfig" %% "pureconfig" % "0.12.2",
  "com.google.cloud" % "google-cloud-storage" % "1.113.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.10",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion,
  "io.chrisdavenport" %% "log4cats-slf4j" % Log4CatsVersion,
  "org.slf4j"    % "slf4j-simple"   % Slf4jVersion,
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds")

fork := true