package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import cats.syntax.flatMap._
import scala.collection.JavaConverters._
// ------------------------------------------------------------------------

class Environment[F[_]] {
  implicit val sync: Sync[F] = Sync[F]

  def getEnv: F[Map[String, String]] = sync.delay {
    System.getenv.asScala.toMap
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
