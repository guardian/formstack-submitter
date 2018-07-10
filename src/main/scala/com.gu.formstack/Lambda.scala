package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import com.gu.formstack.utils._
import java.io.{ InputStream, OutputStream }
import org.apache.logging.log4j.scala.Logging
// ------------------------------------------------------------------------

class FormStackLambda extends Logging {

  /** Entry point: we read off the input stream, perform the main action and send the output back to the output stream */
  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit = {
    val in = StreamOps.consume(is)
    val out = main(in).unsafeRunSync
    StreamOps.writeAndClose(os, out)
  }

  /** The main action is trivial, it creates a processor and runs it right away */
  def main(body: String): IO[String] =
    getToken match {
      case Some(oauthToken) =>
        for {
          process <- Process(oauthToken)
          res <- process.run(body)
        } yield res
      case None =>
        logger.error("Missing OAUTH_TOKEN")
        throw new RuntimeException("Missing OAUTH_TOKEN")
    }

  def getToken: Option[String] = {
    Option(System.getenv.get("OAUTH_TOKEN"))
  }

}
