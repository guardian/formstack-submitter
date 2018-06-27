package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
// ------------------------------------------------------------------------

class StreamOps[F[_]](val logger: LoggingService[F]) {
  implicit val sync: Sync[F] = Sync[F]

  def consume(is: InputStream): F[String] = sync.delay {
    val contents = Source.fromInputStream(is).mkString
    is.close()
    contents
  }

  def writeAndClose(os: OutputStream, contents: String): F[Unit] = sync.delay {
    os.write(contents.getBytes(StandardCharsets.UTF_8))
    os.close()
  }
}