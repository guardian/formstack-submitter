package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
// ------------------------------------------------------------------------

class StreamOps[F[_]](val logger: LoggingService[F]) {
  implicit val sync: Sync[F] = Sync[F]

  def consume(is: InputStream): F[String] =
    sync.bracket(sync.pure(is)) { is =>
      val contents = Source.fromInputStream(is).mkString
      sync.pure(contents)
    }(is => sync.delay(is.close()))

  def writeAndClose(os: OutputStream, contents: String): F[Unit] =
    sync.bracket(sync.pure(os)) { os =>
      sync.delay(os.write(contents.getBytes(StandardCharsets.UTF_8)))
    }(os => sync.delay(os.close()))
}
