package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.{ Context, LambdaLogger }
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

trait IOLambda {
  def main(input: String, logger: LambdaLogger): IO[String]

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val logger = ctx.getLogger
    val out = main(in, logger).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }
}
