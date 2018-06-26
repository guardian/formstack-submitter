package com.gu.formstack

// ------------------------------------------------------------------------
import cats.Apply
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import java.io.{ InputStream, OutputStream }
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process[F[_]: Effect] private (
  val streamOps: StreamOps[F],
  val submitter: FormstackSubmitter[F],
  val requestBody: RequestBody[F]
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
    Apply[F].map2(env.getToken, Http1Client()) { case (oauthToken: String, httpClient: Client[F]) =>
      val streamOps = new StreamOps[F]
      val requestBody = new RequestBody[F]
      val submitter = new FormstackSubmitter(httpClient, oauthToken)

      new Process(streamOps, submitter, requestBody)
    }
  }
}