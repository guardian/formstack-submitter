package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import com.gu.formstack.utils._
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

class FormStackLambda {

  /** Entry point: we read off the input stream, perform the main action and send the output back to the output stream */
  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val logger = ctx.getLogger
    val out = main(in, logger).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }

  /** The main action is trivial, it creates a processor and runs it right away */
  def main(body: String, logger: LambdaLogger): IO[String] =
    Environment.getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process(oauthToken, logger)
          res <- process.run(body)
        } yield res
      case None => throw new RuntimeException("Missing OAUTH_TOKEN")
    }

}
