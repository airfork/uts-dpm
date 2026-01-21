package com.tunjicus.utsdpm.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
  val baseUrl: String,
  val w2wUser: String,
  val w2wPass: String,
  val w2wKey: String,
  val mailgunKey: String,
  val autogenMockEnabled: Boolean = false,
)
