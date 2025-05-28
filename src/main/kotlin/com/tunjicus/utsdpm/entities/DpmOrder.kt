package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.ZonedDateTime

@Entity
@Table(name = "dpm_order")
class DpmOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dpm_order_id")
  var id: Int? = null

  @Column(name = "dpm_order") var dpmOrder: String? = null

  @Column(name = "created_at", updatable = false)
  var createdAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "updated_at") var updatedAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @PrePersist
  @PreUpdate
  fun updateTimestamp() {
    updatedAt = TimeService.getTodayZonedDateTime()
  }

  final override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    val oEffectiveClass =
        if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass
        else other.javaClass
    val thisEffectiveClass =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass
        else this.javaClass
    if (thisEffectiveClass != oEffectiveClass) return false
    other as Dpm

    return id != null && id == other.id
  }

  final override fun hashCode(): Int =
      if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode()
      else javaClass.hashCode()

  final override fun toString(): String {
    return this::class.simpleName +
        "(id = $id, dpmOrder = $dpmOrder, createdAt = $createdAt, updatedAt = $updatedAt)"
  }
}
