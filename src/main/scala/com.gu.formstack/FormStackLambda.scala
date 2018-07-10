package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.{ Effect, IO }
import com.amazonaws.services.lambda.runtime.Context
import com.gu.formstack.utils._
import java.io.{ InputStream, OutputStream }
import org.apache.logging.log4j.scala.Logging
import org.http4s.client.dsl
// ------------------------------------------------------------------------

class FormStackLambda extends Logging with Lambda[IO] {

  implicit val F = Effect[IO]
  implicit val DSL = dsl.io

  /** Entry point: we read off the input stream, perform the main action and send the output back to the output stream */
  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val out = main(in).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }

}
