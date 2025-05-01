package com.tunjicus.utsdpm.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.email")
data class EmailProperties(
    val from: String,
    val domain: String,
    val override: String,
)
