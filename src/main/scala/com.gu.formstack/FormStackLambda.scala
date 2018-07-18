package com.gu.formstack

// ------------------------------------------------------------------------
import com.amazonaws.services.lambda.runtime.Context
import com.gu.formstack.utils._
import java.io.{ InputStream, OutputStream }
import org.apache.logging.log4j.scala.Logging
// ------------------------------------------------------------------------

class FormStackLambda extends Logging with Lambda with StreamOps {

  /** Entry point: we read off the input stream, perform the main action and send the output back to the output stream */
  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = consume(is)
    val out = main(in).unsafeRunSync
    writeAndClose(os, out)
  }

}
