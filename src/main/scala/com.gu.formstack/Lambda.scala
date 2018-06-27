package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

class FormStackLambda {

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val logger = ctx.getLogger
    val out = main(in, logger).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }

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
