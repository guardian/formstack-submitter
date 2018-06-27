package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import io.circe.Json
import io.circe.parser.parse
// ------------------------------------------------------------------------

class RequestBody(logger: LoggingService) {

  def decode(body: String): IO[Json] = IO.suspend {
    logger.info(s"And here we have $body")
    parse(body) match {
      case Left(e) =>
        logger.error(s"The payload isn't valid JSON:\n$body", e)
        IO.raiseError(e)
      case Right(json) => IO.pure(json)
    }
  }
}
