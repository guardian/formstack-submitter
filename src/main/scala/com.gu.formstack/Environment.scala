package com.gu.formstack

// ------------------------------------------------------------------------
// ------------------------------------------------------------------------

object Environment {
  def getToken: Option[String] = {
    val env = System.getenv
    Option(env.get("OAUTH_TOKEN"))
  }
}
