package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.gu.formstack.utils.Settings
import io.circe.Json
import io.circe.parser.parse
import org.apache.logging.log4j.scala.Logging
import org.http4s.client.blaze.{ Http1Client }
// ------------------------------------------------------------------------

class Process private (
  val submitter: FormstackSubmitter
)
    extends Logging {

  /** First we decode the request body into a valid JSON object and then submit it to FormStack, returning whatever we got back */
  def run(body: String): IO[String] =
    for {
      json <- decode(body)
      resp <- submitter.transmit(json)
    } yield resp.noSpaces

  /** In Lambda proxy mode, API Gateway wraps the incoming request in a JSON object that looks like this:
   * {
   *   "resource": "Resource path",
   *   "path": "Path parameter",
   *   "httpMethod": "Incoming request's method name"
   *   "headers": {Incoming request headers}
   *   "queryStringParameters": {query string parameters }
   *   "pathParameters":  {path parameters}
   *   "stageVariables": {Applicable stage variables}
   *   "requestContext": {Request context, including authorizer-returned key-value pairs}
   *   "body": "A JSON string of the request payload."
   *   "isBase64Encoded": "A boolean flag to indicate if the applicable request payload is Base64-encode"
   * }
   *
   * The decoding process extracts the content of the body field, which is what we really need.
   */
  def decode(body: String): IO[Json] = IO.suspend {
    (for {
      json <- parse(body)
      field <- json.hcursor.downField("body").as[String]
      jsonBody <- parse(field)
    } yield jsonBody) match {
      case Left(e) =>
        logger.warn(s"The payload isn't valid JSON:\n$body", e)
        IO.raiseError(e)
      case Right(json) => IO.pure(json)
    }
  }

}

object Process {

  /** Creating an HTTP client is an action so the whole process itself becomes an action */
  def apply(settings: Settings): IO[Process] =
    Http1Client[IO]() map { httpClient =>
      val submitter = new FormstackSubmitter(httpClient, settings)
      new Process(submitter)
    }
}
