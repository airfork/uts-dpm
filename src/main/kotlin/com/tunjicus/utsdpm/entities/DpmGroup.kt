package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.ZonedDateTime

@Entity
@Table(name = "dpm_groups")
class DpmGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dpm_group_id")
  var id: Int? = null

  @Column(name = "group_name", nullable = false, length = 500) lateinit var groupName: String

  @Column(name = "active", nullable = false) var active: Boolean = true

  @Column(name = "created_at", updatable = false)
  var createdAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "updated_at") var updatedAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @OneToMany(
      mappedBy = "dpmGroup",
      fetch = FetchType.EAGER,
      cascade = [CascadeType.ALL],
  )
  var dpms: MutableList<Dpm>? = mutableListOf()

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
    other as DpmGroup

    return id != null && id == other.id
  }

  final override fun hashCode(): Int =
      if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode()
      else javaClass.hashCode()

  final override fun toString(): String {
    return this::class.simpleName +
        "(id = $id, groupName = $groupName, dpmCount = ${dpms?.size}, createdAt = $createdAt, updatedAt = $updatedAt)"
  }
}
