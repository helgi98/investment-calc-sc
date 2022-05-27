import sbt.Keys._
import sbt._

object Common {
  // Dependency versions
  private val doobieVersion = "1.0.0-RC1"
  private val flywayVersion = "7.8.1"
  private val http4sVersion = "0.23.12"
  private val oauthJwtVersion = "3.15.0"
  private val pureConfigVersion = "0.17.1"

  // Transient dependency versions
  // ~ doobie
  private val h2Version = "1.4.200"
  private val postgresVersion = "42.2.19"
  // ~ http4s
  private val circeVersion = "0.14.2"
  private val logbackVersion = "1.2.11"
  private val specs2Version = "4.10.6"

  // Compiler plugin dependency versions
  private val kindProjectorVersion = "0.11.3"

  private[this] def projectSettings = Seq(
    name := "invest-calc"
  )

  final val settings: Seq[Setting[_]] =
    projectSettings ++ dependencySettings ++ compilerPlugins

  private[this] def dependencySettings = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      // Optional for auto-derivation of JSON codecs
      "io.circe" %% "circe-generic" % circeVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion,
    )
  )

  private[this] def compilerPlugins = Seq(
  )
}
