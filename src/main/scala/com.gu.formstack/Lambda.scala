package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.LambdaLogger
// ------------------------------------------------------------------------

class FormStackLambda extends IOLambda {

  def main(body: String, logger: LambdaLogger): IO[String] =
    Environment.getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process[IO](oauthToken, logger)
          res <- process.run(body)
        } yield res
      case None => throw new RuntimeException("Missing OAUTH_TOKEN")
    }

}
