import sbtrelease._,ReleaseStateTransformations._
import xerial.sbt.Sonatype._
import scoverage._

lazy val baseSettings = Seq(
  organization := "vaultscala",
  crossScalaVersions := Seq("2.11.8", "2.12.1"), 
  scalaVersion := crossScalaVersions.value.head, 
  startYear := Some(2014), 
  isSnapshot := version.value.trim.endsWith("SNAPSHOT"), 
  scalaVersion := "2.12.1",
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  description := "Scala Vault client.",
  publishMavenStyle := true,
    publishTo := {
      val nexus =
        "https://oss.sonatype.org/"
      if (version.value.trim
            .endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
  publishArtifact in Test := false,
  scmInfo := Some(ScmInfo(
    url("https://github.com/y2k2mt/vault-scala"),
    "scm:git:git@github.com:y2k2mt/vault-scala.git"
  )),
  pomExtra := (
    <url>http://github.com/y2k2mt</url>
      <developers>
        <developer>
          <id>y2k2mt</id>
          <name>y2k2mt</name>
          <url>https://github.com/y2k2mt</url>
        </developer>
      </developers>
    ),
  isSnapshot := true,
  scalacOptions ++= Seq(
    "-deprecation",
    "-Yrangepos",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ydelambdafy:method",
    "-target:jvm-1.8"
  )
)

val additionalSettings = 
  ReleasePlugin.projectSettings ++ sonatypeSettings ++ ScoverageSbtPlugin.projectSettings

val allResolvers = Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("snapshots")
)

val specs2V = "4.0.2"
val allDependencies = Seq(
  "org.specs2" %% "specs2-core" % specs2V % "test",
  "org.specs2" %% "specs2-mock" % specs2V % "test"
)

lazy val core = (project in file("core")).
  settings(baseSettings: _*).
  settings(additionalSettings: _*).
  settings(
    name := "vaultscala-core",
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies
  )
lazy val akkaHttp = (project in file("akka-http")).
  dependsOn(core).
  settings(baseSettings: _*).
  settings(additionalSettings: _*).
  settings(
    name := "vaultscala-akka",
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies ++ Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.11"
    )
  )
lazy val json4s = (project in file("json4s")).
  dependsOn(core).
  settings(baseSettings: _*).
  settings(additionalSettings: _*).
  settings(
    name := "vaultscala-json4s",
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies ++ Seq(
      "org.json4s" %% "json4s-jackson" % "3.5.2"
    )
  )
