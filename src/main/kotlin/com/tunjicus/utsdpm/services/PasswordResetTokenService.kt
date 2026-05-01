package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.entities.PasswordResetToken
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.exceptions.PasswordChangeException
import com.tunjicus.utsdpm.repositories.PasswordResetTokenRepository
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.Base64
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordResetTokenService(
  private val passwordResetTokenRepository: PasswordResetTokenRepository
) {
  fun createToken(user: User): String {
    val token = generateToken()
    passwordResetTokenRepository.save(
      PasswordResetToken().apply {
        this.user = user
        tokenHash = hashToken(token)
        expiresAt = TimeService.getTodayZonedDateTime().plusHours(TOKEN_TTL_HOURS)
      })
    return token
  }

  @Transactional
  fun consumeToken(token: String): User {
    val resetToken =
      passwordResetTokenRepository.findByTokenHash(hashToken(token))
        ?: throw invalidTokenException()

    val now = TimeService.getTodayZonedDateTime()
    if (resetToken.usedAt != null || resetToken.expiresAt.isBefore(now)) {
      throw invalidTokenException()
    }

    resetToken.usedAt = now
    return resetToken.user
  }

  private fun invalidTokenException() =
    PasswordChangeException("Password reset token is invalid or expired")

  companion object {
    private const val TOKEN_BYTE_LENGTH = 32
    private const val TOKEN_TTL_HOURS = 1L
    private val SECURE_RANDOM = SecureRandom()

    private fun generateToken(): String {
      val bytes = ByteArray(TOKEN_BYTE_LENGTH)
      SECURE_RANDOM.nextBytes(bytes)
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
      val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
      return digest.joinToString("") { "%02x".format(it) }
    }
  }
}
