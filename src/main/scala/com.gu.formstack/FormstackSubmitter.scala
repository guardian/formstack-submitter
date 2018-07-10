package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import cats.syntax.applicativeError._
import io.circe.Json
import org.apache.logging.log4j.scala.Logging
import org.http4s.{ AuthScheme, Credentials, Header, Response, Uri }
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.Method.POST
// ------------------------------------------------------------------------

class FormstackSubmitter(httpClient: Client[IO], oauthToken: String) extends Logging {

  /** The form submission API is accessible at /form/:formId/submission.json
   * - The formId is extracted
   * - The HTTP call is created
   * - ... then sent, the response being post-processed to be API Gateway
   *   compliant
   */
  def transmit(json: Json): IO[Json] =
    for {
      formId <- getFormId(json)
      request <- POST(endpoint(formId), json)
      response <- httpClient.fetch(request.putHeaders(header))(format)
    } yield response

  /** Extract formId from JSON payload */
  private def getFormId(json: Json): IO[String] =
    json.hcursor.downField("formId").as[String] match {
      case Left(e) =>
        logger.error("Missing `formId` in request payload", e)
        IO.raiseError(e)
      case Right(formId) => IO.pure(formId)
    }

  /** Transform a response into valid API gateway format:
   * {
   *   "isBase64Encoded": Bool,
   *   "statusCode": String,
   *   "body": String
   * }
   */
  private def format(response: Response[IO]): IO[Json] =
    response
      .as[Json]
      .handleErrorWith { e =>
        logger.error("FormStack did not return valid JSON", e)
        IO.pure(Json.Null)
      }
      .map(
        json =>
          Json.obj(
            "isBase64Encoded" -> Json.False,
            "statusCode" -> Json.fromInt(response.status.code),
            "body" -> Json.fromString(json.noSpaces)
        )
      )

  private def endpoint(formId: String): Uri =
    Uri.uri("https://www.formstack.com/api/v2/form/") / formId / "submission.json"

  private final val header: Header = Authorization(Credentials.Token(AuthScheme.Bearer, oauthToken))
}
