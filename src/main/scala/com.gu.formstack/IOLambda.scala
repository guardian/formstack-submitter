package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

trait IOLambda {
  def main(input: String): IO[String]

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val out = main(in).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }
}
