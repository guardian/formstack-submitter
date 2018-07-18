package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.gu.formstack.utils.Settings
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
import scala.collection.JavaConverters._
// ------------------------------------------------------------------------

trait Lambda {

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit

  /** The main action is trivial, it creates a processor and runs it right away */
  def main(body: String): IO[String] =
    Settings(System.getenv.asScala.toMap).map { settings =>
      for {
        process <- Process(settings)
        res <- process.run(body)
      } yield res
    }.valueOr(msgs => IO.raiseError(new RuntimeException(msgs.reduceLeft(_ + "\n" + _))))
}
