package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
// import cats.syntax.flatMap._
// ------------------------------------------------------------------------

class Environment[F[_]] {
  implicit val sync: Sync[F] = Sync[F]

  def getEnv: F[java.util.Map[String, String]] = sync.pure {
    System.getenv()
  }

  def getToken: F[String] = {
    // getEnv.flatMap { env: java.util.Map[String, String] =>
    val env = System.getenv
    Option(env.get("OAUTH_TOKEN")) match {
      case Some(oauth) => Sync[F].pure(oauth)
      case None =>
        Sync[F].raiseError(new RuntimeException("Missing OAUTH_TOKEN"))
    }
  }
}
