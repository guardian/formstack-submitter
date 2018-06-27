package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
// ------------------------------------------------------------------------

class FormStackLambda extends IOLambda {

  def main(is: InputStream, os: OutputStream, ctx: Context): IO[Unit] =
    for {
      process <- Process[IO]
      _ <- process.run(is, os)
    } yield ()

}
