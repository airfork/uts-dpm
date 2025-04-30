package com.tunjicus.utsdpm.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.jwt")
data class JwtProperties(
    val secret: String,
    val expirationMs: Long,
    val cookieName: String,
)
