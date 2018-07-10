package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
import org.http4s.client.dsl.Http4sClientDsl
// ------------------------------------------------------------------------

trait Lambda[F[_]] {

  implicit val F: Effect[F]
  implicit val DSL: Http4sClientDsl[F]

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit

  /** The main action is trivial, it creates a processor and runs it right away */
  def main(body: String): F[String] =
    getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process(oauthToken)
          res <- process.run(body)
        } yield res
      case None =>
        F.raiseError(new RuntimeException("Missing OAUTH_TOKEN"))
    }

  def getToken: Option[String] = {
    Option(System.getenv.get("OAUTH_TOKEN"))
  }
}