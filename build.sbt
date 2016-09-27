import sbtrelease._,ReleaseStateTransformations._
import xerial.sbt.Sonatype._
import scoverage._

lazy val baseSettings = Seq(
  organization := "vaultscala",
  scalaVersion := "2.11.8",
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  description := "Scala Vault client.",
  publishMavenStyle := true,
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
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
    "-Ywarn-unused",
    "-Ybackend:GenBCode",
    "-Ydelambdafy:method",
    "-target:jvm-1.8"
  )
)

val additionalSettings = 
  Defaults.defaultSettings ++ ReleasePlugin.projectSettings ++ sonatypeSettings ++ ScoverageSbtPlugin.projectSettings ++ SbtScalariform.defaultScalariformSettings

val allResolvers = Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("snapshots")
)

val specs2V = "3.8.4"
val allDependencies = Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
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
  dependsOn("core").
  settings(baseSettings: _*).
  settings(additionalSettings: _*).
  settings(
    name := "vaultscala-akka",
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies ++ Seq(
      "com.typesafe.akka" %% "akka-http-core" % "2.4.10"
    )
  )
lazy val json4s = (project in file("json4s")).
  dependsOn("core").
  settings(baseSettings: _*).
  settings(additionalSettings: _*).
  settings(
    name := "vaultscala-json4s",
    resolvers ++= allResolvers,
    libraryDependencies ++= allDependencies ++ Seq(
      "org.json4s" %% "json4s-jackson" % "3.4.1"
    )
  )
