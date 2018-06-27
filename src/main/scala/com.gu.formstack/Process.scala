package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.flatMap._
import java.io.{ InputStream, OutputStream }
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process[F[_]: Effect] private (
  val streamOps: StreamOps[F],
  val submitter: FormstackSubmitter[F],
  val requestBody: RequestBody[F],
  val logger: LoggingService[F]
) {
  def run(is: InputStream, os: OutputStream): F[Unit] =
    for {
      body <- streamOps.consume(is)
      json <- requestBody.decode(body)
      resp <- submitter.transmit(json)
      _ <- streamOps.writeAndClose(os, resp.noSpaces)
    } yield ()
}

object Process {
  def apply[F[_]: Effect]: F[Process[F]] = {
    val env = new Environment[F]
    (env.getToken, LoggingService("FormstackSubmitter"), Http1Client()).mapN {
      case (oauthToken: String, logger: LoggingService[F], httpClient: Client[F]) =>
        val streamOps = new StreamOps[F](logger)
        val requestBody = new RequestBody[F](logger)
        val submitter = new FormstackSubmitter(httpClient, oauthToken, logger)

        new Process(streamOps, submitter, requestBody, logger)
    }
  }
}
