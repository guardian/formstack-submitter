package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

trait IOLambda {
  def main(is: InputStream, os: OutputStream, ctx: Context): IO[Unit]

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit =
    main(is, os, ctx).unsafeRunSync
}
