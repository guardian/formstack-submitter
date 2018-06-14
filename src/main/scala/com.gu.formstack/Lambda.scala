package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context;
import java.io.{InputStream, OutputStream}
// ------------------------------------------------------------------------

class FormStackLambda extends GenericLambda[IO] {

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit =
    run(is, os).unsafeRunSync

}
