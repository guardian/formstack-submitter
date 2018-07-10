import sbt._

object Dependencies {
  val awsVersion = "1.1.0"
  val circeVersion = "0.9.3"
  val http4sVersion = "0.18.15"

  lazy val circe = Seq(
    "circe-core",
    "circe-generic",
    "circe-parser"
  ).map("io.circe" %% _ % circeVersion)

  lazy val aws = Seq(
    "aws-lambda-java-core",
    "aws-lambda-java-log4j2"
  ).map("com.amazonaws" % _ % awsVersion)

  lazy val http4s = Seq(
    "http4s-dsl",
    "http4s-blaze-client",
    "http4s-circe"
  ).map("org.http4s" %% _ % http4sVersion)

}
