package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

class FormStackLambda extends IOLambda {

  def main(is: InputStream, os: OutputStream, ctx: Context): IO[Unit] =
    Environment.getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process[IO](oauthToken)
          _ <- process.run(is, os)
        } yield ()
      case None => throw new RuntimeException("Missing OAUTH_TOKEN")
    }

}
