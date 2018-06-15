package com.gu.formstack.utils

// ------------------------------------------------------------------------
import cats.effect.Sync
import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
// ------------------------------------------------------------------------

object StreamOps {
  implicit class InputStreamOps(val is: InputStream) extends AnyVal {
    def consume[F[_]: Sync](): F[String] = Sync[F].delay {
      val contents = Source.fromInputStream(is).mkString
      is.close()
      contents
    }
  }

  implicit class OutputStreamOps(val os: OutputStream) extends AnyVal {
    def writeAndClose[F[_]: Sync](contents: String): F[Unit] = Sync[F].delay {
      os.write(contents.getBytes(StandardCharsets.UTF_8))
      os.close()
    }
  }
}
