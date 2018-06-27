package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.amazonaws.services.lambda.runtime.LambdaLogger
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process[F[_]: Effect] private (
  val submitter: FormstackSubmitter[F],
  val requestBody: RequestBody[F],
  val logger: LoggingService
) {
  def run(body: String): F[String] = {
    logger.info(s"Received $body")
    for {
      json <- requestBody.decode(body)
      resp <- submitter.transmit(json)
    } yield resp.noSpaces
  }
}

object Process {
  def apply[F[_]: Effect](oauthToken: String, logger: LambdaLogger): F[Process[F]] = {
    val log = new LoggingService(logger)
    for {
      httpClient <- Http1Client()
    } yield {
      val requestBody = new RequestBody[F](log)
      val submitter = new FormstackSubmitter(httpClient, oauthToken, log)

      new Process(submitter, requestBody, log)
    }
  }
}
