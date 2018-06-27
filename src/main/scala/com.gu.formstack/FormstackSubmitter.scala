package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.flatMap._
import io.circe.Json
import org.http4s.{ AuthScheme, Credentials, Header, Response, Uri }
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Authorization
import org.http4s.Method.POST
// ------------------------------------------------------------------------

class FormstackSubmitter[F[_]: Effect](httpClient: Client[F], oauthToken: String, logger: LoggingService[F])
    extends Http4sClientDsl[F] {
  import FormstackSubmitter._

  def transmit(json: Json): F[Json] =
    for {
      formId <- getFormId(json)
      request <- POST(endpoint(formId), json)
      response <- httpClient.fetch(request.putHeaders(header(oauthToken)))(format)
    } yield response

  private def getFormId(json: Json): F[String] =
    json.hcursor.downField("formId").as[String] match {
      case Left(error)   => Effect[F].raiseError(error)
      case Right(formId) => Effect[F].pure(formId)
    }

  private def format(response: Response[F]): F[Json] =
    response
      .as[Json] // can break
      .map(
        json =>
          Json.obj(
            "isBase64Encoded" -> Json.False,
            "statusCode" -> Json.fromInt(response.status.code),
            "body" -> Json.fromString(json.noSpaces)
        )
      )
}

private object FormstackSubmitter {
  def endpoint(formId: String): Uri =
    Uri.uri("https://www.formstack.com/api/v2/form/") / formId / "submission.json"

  def header(oauth: String): Header =
    Authorization(Credentials.Token(AuthScheme.Bearer, oauth))
}
