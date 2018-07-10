package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.IO
import com.amazonaws.services.lambda.runtime.LambdaLogger
import io.circe.Json
import io.circe.parser.parse
import org.http4s.client.blaze.Http1Client
// ------------------------------------------------------------------------

class Process private (
  val submitter: FormstackSubmitter,
  val requestBody: RequestBody,
  val logger: LoggingService
) {
  def run(body: String): IO[String] = 
    for {
      json <- requestBody.decode(body)
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
        logger.error(s"The payload isn't valid JSON:\n$body", e)
        IO.raiseError(e)
      case Right(json) => IO.pure(json)
    }
  }

}

object Process {

  /** Creating an HTTP client is an action so the whole process itself becomes an action */
  def apply(oauthToken: String, logger: LambdaLogger): IO[Process] =
    Http1Client[IO]() map { httpClient =>
    val log = new LoggingService(logger)
      val submitter = new FormstackSubmitter(httpClient, oauthToken, log)

      new Process(submitter, log)
  }
}
