package com.gu.formstack.utils

// ------------------------------------------------------------------------
import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
// ------------------------------------------------------------------------

trait StreamOps {
  def consume(is: InputStream): String = {
    val contents = Source.fromInputStream(is).mkString
    is.close()
    contents
  }

  def writeAndClose(os: OutputStream, contents: String): Unit = {
    os.write(contents.getBytes(StandardCharsets.UTF_8))
    os.close()
  }
}
