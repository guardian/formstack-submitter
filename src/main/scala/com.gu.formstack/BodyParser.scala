package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import io.circe.Json
import io.circe.parser.parse
// ------------------------------------------------------------------------

class RequestBody[F[_]](logger: LoggingService) {
  implicit val sync: Sync[F] = Sync[F]

  def decode(body: String): F[Json] = sync.suspend {
    logger.info(s"And here we have $body")
    parse(body) match {
      case Left(e) =>
        logger.error(s"The payload isn't valid JSON:\n$body", e)
        sync.raiseError(e)
      case Right(json) => sync.pure(json)
    }
  }
}
