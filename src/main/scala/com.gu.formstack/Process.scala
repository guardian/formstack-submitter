package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.gu.formstack.utils.Settings
import io.circe.Json
import io.circe.parser.parse
import org.apache.logging.log4j.scala.Logging
import org.http4s.client.blaze.{ Http1Client, BlazeClientConfig }
import org.http4s.client.dsl.Http4sClientDsl
// ------------------------------------------------------------------------

class Process[F[_]] private (
  val submitter: FormstackSubmitter[F]
)(implicit F: Effect[F])
    extends Logging {

  /** First we decode the request body into a valid JSON object and then submit it to FormStack, returning whatever we got back */
  def run(body: String): F[String] =
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
  def decode(body: String): F[Json] = F.suspend {
    (for {
      json <- parse(body)
      field <- json.hcursor.downField("body").as[String]
      jsonBody <- parse(field)
    } yield jsonBody) match {
      case Left(e) =>
        logger.warn(s"The payload isn't valid JSON:\n$body", e)
        F.raiseError(e)
      case Right(json) => F.pure(json)
    }
  }

}

object Process {

  /** Creating an HTTP client is an action so the whole process itself becomes an action */
  def apply[F[_]: Effect: Http4sClientDsl](settings: Settings): F[Process[F]] =
    Http1Client[F](BlazeClientConfig.defaultConfig) map { httpClient =>
      val submitter = new FormstackSubmitter(httpClient, settings)
      new Process(submitter)
    }
}
