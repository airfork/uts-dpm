package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import java.time.ZonedDateTime
import org.hibernate.proxy.HibernateProxy

@Entity
@Table(name = "w2w_colors")
class W2WColor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "w2w_color_id")
  var id: Int? = null

  @Column(name = "color_code", nullable = false, length = 10) lateinit var colorCode: String

  @Column(name = "color_name", nullable = false, length = 100) lateinit var colorName: String

  @Column(name = "hex_code", nullable = false, length = 5, columnDefinition = "bpchar")
  lateinit var hexCode: String

  @Column(name = "active", nullable = false) var active: Boolean = true

  @Column(name = "created_at", updatable = false)
  var createdAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "updated_at") var updatedAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @OneToMany(mappedBy = "w2wColor", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
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
    other as W2WColor

    return id != null && id == other.id
  }

  final override fun hashCode(): Int =
      if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode()
      else javaClass.hashCode()

  final override fun toString(): String {
    return this::class.simpleName +
        "(id = $id , colorCode = $colorCode , colorName = $colorName , active = $active , createdAt = $createdAt , updatedAt = $updatedAt )"
  }
}
