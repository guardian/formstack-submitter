package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process[F[_]: Effect] private (
  val submitter: FormstackSubmitter[F],
  val requestBody: RequestBody[F],
  val logger: LoggingService[F]
) {
  def run(body: String): F[String] =
    for {
      json <- requestBody.decode(body)
      resp <- submitter.transmit(json)
    } yield resp.noSpaces
}

object Process {
  def apply[F[_]: Effect](oauthToken: String): F[Process[F]] = {
    for {
      logger <- LoggingService("FormstackSubmitter")
      httpClient <- Http1Client()
    } yield {
      val requestBody = new RequestBody[F](logger)
      val submitter = new FormstackSubmitter(httpClient, oauthToken, logger)

      new Process(submitter, requestBody, logger)
    }
  }
}
