package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Int> {
  fun findByTokenHash(tokenHash: String): PasswordResetToken?
}
