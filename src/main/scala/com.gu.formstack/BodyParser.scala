package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import io.circe.Json
import io.circe.parser.parse
// ------------------------------------------------------------------------

class RequestBody[F[_]] {
  implicit val sync: Sync[F] = Sync[F]

  def decode(body: String): F[Json] = sync.suspend {
    parse(body) match {
      case Left(error) => sync.raiseError(error)
      case Right(json) => sync.pure(json)
    }
  }
}