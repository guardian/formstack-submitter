package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
// ------------------------------------------------------------------------

class FormStackLambda extends IOLambda {

  def main(body: String): IO[String] =
    Environment.getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process[IO](oauthToken)
          res <- process.run(body)
        } yield res
      case None => throw new RuntimeException("Missing OAUTH_TOKEN")
    }

}
