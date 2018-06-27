package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.LambdaLogger
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process private (
  val submitter: FormstackSubmitter,
  val requestBody: RequestBody,
  val logger: LoggingService
) {
  def run(body: String): IO[String] = {
    logger.info(s"Received $body")
    for {
      json <- requestBody.decode(body)
      resp <- submitter.transmit(json)
    } yield resp.noSpaces
  }
}

object Process {
  def apply(oauthToken: String, logger: LambdaLogger): IO[Process] = {
    val log = new LoggingService(logger)
    for {
      httpClient <- Http1Client[IO]()
    } yield {
      val requestBody = new RequestBody(log)
      val submitter = new FormstackSubmitter(httpClient, oauthToken, log)

      new Process(submitter, requestBody, log)
    }
  }
}
