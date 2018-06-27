package com.gu.formstack

// ------------------------------------------------------------------------
import cats.effect.Sync
import org.apache.logging.log4j.{ LogManager, Logger }
// ------------------------------------------------------------------------

trait LoggingService[F[_]] {
  def logger: Logger
  def sync: Sync[F]

  def info(msg: String): F[Unit] = sync.delay {
    logger.info(msg)
  }

  def warn(msg: String): F[Unit] = sync.delay {
    logger.warn(msg)
  }

  def error(msg: String): F[Unit] = sync.delay {
    logger.error(msg)
  }
}

object LoggingService {
  def apply[F[_]: Sync](name: String): F[LoggingService[F]] = Sync[F].delay {
    new LoggingService[F] {
      val logger = LogManager.getLogger(name)
      val sync = Sync[F]
    }
  }
}
