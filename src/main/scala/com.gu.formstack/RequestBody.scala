package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import io.circe.Json
import io.circe.parser.parse
// ------------------------------------------------------------------------

class RequestBody(logger: LoggingService) {

  def decode(body: String): IO[Json] = IO.suspend {
    (for {
      json <- parse(body)
      field <- json.hcursor.downField("body").as[String]
      jsonBody <- parse(field)
    } yield jsonBody) match {
      case Left(e) =>
        logger.error(s"The payload isn't valid JSON:\n$body", e)
        IO.raiseError(e)
      case Right(json) => IO.pure(json)
    }
  }
}
