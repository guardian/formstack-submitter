package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.gu.formstack.utils.Settings
import com.amazonaws.services.lambda.runtime.Context
import java.io.{ InputStream, OutputStream }
import org.http4s.client.dsl.Http4sClientDsl
import scala.collection.JavaConverters._
// ------------------------------------------------------------------------

trait Lambda[F[_]] {

  implicit val F: Effect[F]
  implicit val DSL: Http4sClientDsl[F]

  def handle(is: InputStream, os: OutputStream, ctx: Context): Unit

  /** The main action is trivial, it creates a processor and runs it right away */
  def main(body: String): F[String] =
    Settings(System.getenv.asScala.toMap) match {
      case Right(settings) =>
        for {
          process <- Process(settings)
          res <- process.run(body)
        } yield res
      case Left(msg) =>
        F.raiseError(new RuntimeException(msg))
    }
}
