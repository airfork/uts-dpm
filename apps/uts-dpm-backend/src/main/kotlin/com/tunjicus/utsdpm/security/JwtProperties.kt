package com.tunjicus.utsdpm.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@ConfigurationProperties("app.jwt")
@Component
class JwtProperties {
  lateinit var secret: String
  lateinit var cookieName: String
  var expirationMs by Delegates.notNull<Long>()
}
