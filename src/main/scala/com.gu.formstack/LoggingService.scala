package com.gu.formstack

// ------------------------------------------------------------------------
import com.amazonaws.services.lambda.runtime.LambdaLogger
// ------------------------------------------------------------------------

class LoggingService(logger: LambdaLogger) {

  def info(msg: String): Unit = logger.log(s"[INFO] $msg\n")

  def warn(msg: String): Unit = logger.log(s"[WARN] $msg\n")

  def error(msg: String, err: Throwable): Unit = logger.log(s"[ERROR] $msg:\n$err\n")
}
