package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.{ Effect, Sync }
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.circe.{ Decoder, Json }
import io.circe.parser.parse
import io.circe.syntax._
import java.io.{ InputStream, OutputStream }
import java.nio.charset.StandardCharsets
import org.http4s.{ AuthScheme, Credentials, Header, Request, Response, Uri }
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.{ `Access-Control-Allow-Origin`, Authorization }
import org.http4s.Method.POST
import scala.collection.JavaConverters._
import scala.io.Source
// ------------------------------------------------------------------------

abstract class GenericLambda[F[_]: Effect] extends Http4sClientDsl[F] {
  import GenericLambda._

  def run(is: InputStream, os: OutputStream): F[Unit] =
    for {
      oauthToken <- getToken
      body <- consume(is)
      json <- decode(body)
      resp <- transmit(json, oauthToken)
      _ <- writeAndClose(os, resp.noSpaces)
    } yield ()

  def getEnv: F[Map[String, String]] = Effect[F].delay {
    System.getenv.asScala.toMap
  }

  def getToken: F[String] =
    getEnv.flatMap { env: Map[String, String] =>
      env.get("OAUTH_TOKEN") match {
        case Some(oauth) => Effect[F].pure(oauth)
        case None =>
          Effect[F].raiseError(new RuntimeException("Missing OAUTH_TOKEN"))
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
      case Left(error)   => Effect[F].raiseError(error)
      case Right(formId) => Effect[F].pure(formId)
    }

  def transmit(json: Json, oauthToken: String): F[Json] =
    for {
      httpClient <- Http1Client[F]()
      formId <- getFormId(json)
      request <- POST(endpoint(formId), json)
      response <- httpClient.fetch(request.putHeaders(header(oauthToken)))(format)
    } yield response

  def format(response: Response[F]): F[Json] =
    response
      .as[Json]
      .map(
        json =>
          Json.obj(
            "isBase64Encoded" -> Json.False,
            "statusCode" -> Json.fromInt(response.status.code),
            "body" -> Json.fromString(json.noSpaces)
        )
      )
}

object GenericLambda {
  def endpoint(formId: String): Uri =
    Uri.uri("https://www.formstack.com/api/v2/form/") / formId / "submission.json"

  def header(oauth: String): Header =
    Authorization(Credentials.Token(AuthScheme.Bearer, oauth))

  def consume[F[_]: Sync](is: InputStream): F[String] = Sync[F].delay {
    val contents = Source.fromInputStream(is).mkString
    is.close()
    contents
  }

  def writeAndClose[F[_]: Sync](os: OutputStream, contents: String): F[Unit] = Sync[F].delay {
    os.write(contents.getBytes(StandardCharsets.UTF_8))
    os.close()
  }

}
