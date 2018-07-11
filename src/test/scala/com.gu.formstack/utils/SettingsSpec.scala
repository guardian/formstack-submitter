package com.gu.formstack.utils

import org.scalatest._

class SettingsSpec extends FlatSpec with Matchers with Inspectors {
  private val validEnv = Map(
    "FORMSTACK_URL"      -> "https://localhost",
    "OAUTH_TOKEN"        -> "1234"
  )

  private val invalidUrl = Map(
    "FORMSTACK_URL"      -> "This is nonsense",
    "OAUTH_TOKEN"        -> "1234"
  )

  private val emptyEnv = Map.empty[String, String]

  "Parsing settings" should "produce a valid settings instance" in {
    val result = Settings(validEnv)
    
    result.isValid shouldBe true
    result.fold(
      _ => fail("Hmm, Houston, we should have a Settings here"),
      r => r shouldBe a [Settings]
    )
  }

  it should "catch invalid URL" in {
    val result = Settings(invalidUrl)

    result.isValid shouldBe false
    result.fold(
      es => es.length shouldBe 1,
      _ => fail("Won't happen")
    )
  }

  it should "find all missing keys" in {
    val result = Settings(emptyEnv)

    result.isValid shouldBe false
    result.fold(
      es => es.length shouldBe validEnv.size,
      _ => fail("Won't happen")
    )
  }
}