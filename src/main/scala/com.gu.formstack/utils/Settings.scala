package com.gu.formstack.utils

// ------------------------------------------------------------------------
import cats.data.{ Validated, ValidatedNel }
import cats.implicits._
import org.http4s.Uri
// ------------------------------------------------------------------------

case class Settings(
  endpoint: Uri,
  oauthToken: String
)

object Settings {
  def apply(env: Map[String, String]): ValidatedNel[String, Settings] =
    (Validated
       .fromEither(
         env
           .get("FORMSTACK_URL")
           .fold(Either.left[String, Uri]("Missing FORMSTACK_URL"))(Uri.fromString(_).leftMap(_.toString))
       )
       .toValidatedNel,
     getEnv(env, "OAUTH_TOKEN"))
      .mapN(Settings(_, _))

  private def getEnv(env: Map[String, String], key: String): ValidatedNel[String, String] =
    env.get(key) match {
      case None    => (s"Missing $key").invalidNel
      case Some(x) => x.validNel
    }
}
