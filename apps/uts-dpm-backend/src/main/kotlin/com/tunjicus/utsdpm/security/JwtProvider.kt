package com.tunjicus.utsdpm.security

import com.tunjicus.utsdpm.services.AuthService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class JwtProvider(private val jwtProperties: JwtProperties) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(JwtProvider::class.java)
  }

  private val encodedSecret = jwtProperties.secret.encodeToByteArray()

  fun generateToken(authentication: Authentication): String {
    val userPrincipal = authentication.principal as UserPrincipal
    val now = Date()
    val expiryDate = Date(now.time + jwtProperties.expirationMs)

    return Jwts.builder()
      .setSubject(userPrincipal.username)
      .setIssuedAt(now)
      .setExpiration(expiryDate)
      .signWith(Keys.hmacShaKeyFor(encodedSecret))
      .addClaims(mapOf(Pair("role", AuthService.getRole(authentication.authorities))))
      .compact()
  }

  fun getUserUsernameFromJWT(token: String): String {
    val claims =
      Jwts.parserBuilder().setSigningKey(encodedSecret).build().parseClaimsJws(token).body
    return claims.subject
  }

  fun validateToken(token: String): Boolean {
    return try {
      Jwts.parserBuilder().setSigningKey(encodedSecret).build().parseClaimsJws(token)
      true
    } catch (_: ExpiredJwtException) {
      false
    } catch (ex: Exception) {
      LOGGER.error("Unhandled while validating jwt token", ex)
      false
    }
  }
}
