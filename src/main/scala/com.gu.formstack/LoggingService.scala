package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import com.amazonaws.services.lambda.runtime.LambdaLogger
// ------------------------------------------------------------------------

class LoggingService[F[_]](logger: LambdaLogger) {
  implicit val sync: Sync[F] = Sync[F]

  def info(msg: String): F[Unit] = sync.delay {
    logger.log(s"[INFO] $msg")
  }

  def warn(msg: String): F[Unit] = sync.delay {
    logger.log(s"[WARN] $msg")
  }

  def error(msg: String, err: Throwable): F[Unit] = sync.delay {
    logger.log(s"[ERROR] $msg: $err")
  }
}
