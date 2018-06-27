package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import cats.syntax.flatMap._
import scala.collection.JavaConverters._
// ------------------------------------------------------------------------

class Environment[F[_]] {
  implicit val sync: Sync[F] = Sync[F]

  def getEnv: F[Map[String, String]] = sync.delay {
    Option(System.getenv).map(_.asScala.toMap)
  }.flatMap {
    case None =>
      Sync[F].raiseError(new RuntimeException("Environment is empty"))
    case Some(x) =>
      Sync[F].pure(x)
  }

  def getToken: F[String] =
    getEnv.flatMap { env: Map[String, String] =>
      env.get("OAUTH_TOKEN") match {
        case Some(oauth) => Sync[F].pure(oauth)
        case None =>
          Sync[F].raiseError(new RuntimeException("Missing OAUTH_TOKEN"))
      }
    }
}
