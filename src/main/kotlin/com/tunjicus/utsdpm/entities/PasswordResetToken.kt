package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "password_reset_token_id")
  var id: Int? = null

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  lateinit var user: User

  @Column(name = "token_hash", nullable = false, length = 64, columnDefinition = "bpchar")
  lateinit var tokenHash: String

  @Column(name = "expires_at", nullable = false)
  lateinit var expiresAt: ZonedDateTime

  @Column(name = "used_at") var usedAt: ZonedDateTime? = null

  @Column(name = "created_at", updatable = false)
  var createdAt: ZonedDateTime = TimeService.getTodayZonedDateTime()
}
