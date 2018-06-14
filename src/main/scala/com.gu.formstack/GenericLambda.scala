package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.gu.formstack.utils.StreamOps._
import io.circe.{Json, Decoder}
import io.circe.parser.parse
import io.circe.syntax._
import java.io.{InputStream, OutputStream}
import org.http4s.Header
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method.POST
import scala.collection.JavaConverters._
// ------------------------------------------------------------------------

abstract class GenericLambda[F[_]: Effect] extends Http4sClientDsl[F] {
  import GenericLambda._

  def run(is: InputStream, os: OutputStream): F[Unit] = for {
    oauthToken <- getToken
    body <- is.consume
    json <- decode(body)
    resp <- transmit(json, oauthToken)
    _ <- os.writeAndClose(resp.noSpaces)
  } yield ()

  def getEnv: F[Map[String, String]] = Effect[F].delay {
    System.getenv.asScala.toMap
  }

  def getToken: F[String] =
    getEnv.flatMap { env: Map[String, String] =>
      env.get("OAUTH_TOKEN") match {
        case Some(oauth) => Effect[F].pure(oauth)
        case None => Effect[F].raiseError(new RuntimeException("Missing OAUTH_TOKEN"))
      }
    }

  def decode(body: String): F[Json] = Effect[F].suspend {
    parse(body) match {
      case Left(error) => Effect[F].raiseError(error)
      case Right(json) => Effect[F].pure(json)
    }
  }

  def getFormId(json: Json): F[String] =
    json.hcursor.downField("formId").as[String] match {
      case Left(error) => Effect[F].raiseError(error)
      case Right(formId) => Effect[F].pure(formId)
    }

  def transmit(json: Json, oauthToken: String): F[Json] = for {
    httpClient <- Http1Client[F]()
    formId <- getFormId(json)
    request <- POST(endpoint(formId), json)
    response <- httpClient.expect[Json](request.putHeaders(header(oauthToken)))
  } yield response
}

object GenericLambda {
  def endpoint(formId: String): Uri =
    Uri.uri("https://www.formstack.com/api/v2/form/") / formId / "submission.json"

  def header(oauth: String): Header =
    Header("Authorization", s"Bearer $oauth")
}