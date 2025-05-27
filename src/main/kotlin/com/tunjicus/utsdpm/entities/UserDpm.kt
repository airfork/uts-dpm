package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import org.hibernate.proxy.HibernateProxy

@Entity
@Table(name = "user_dpms")
class UserDpm {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "user_dpm_id") var id: Int? = null

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "create_id")
  var createdUser: User? = null

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  var user: User? = null

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "dpm_id")
  var dpmType: Dpm? = null

  @Column(name = "block", length = 20) var block: String? = null

  @Column(name = "date") var date: LocalDate? = null

  @Column(name = "points", nullable = false, columnDefinition = "int2") var points: Int? = null

  @Column(name = "notes") var notes: String? = null

  @Column(name = "created_at", updatable = false)
  var createdAt: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "approved") var approved: Boolean? = false

  @Column(name = "location") var location: String? = null

  @Column(name = "start_time", nullable = false) var startTime: LocalTime? = null

  @Column(name = "end_time", nullable = false) var endTime: LocalTime? = null

  @Column(name = "ignored") var ignored: Boolean? = false

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
    other as UserDpm

    return id != null && id == other.id
  }

  final override fun hashCode(): Int =
      if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode()
      else javaClass.hashCode()

  final override fun toString(): String {
    return this::class.simpleName +
        "(id = $id, createdUser = ${createdUser?.id}, user = ${user?.id}, dpmType = ${dpmType?.dpmName}, " +
        "block = $block, date = $date, points = $points, notes = $notes, created = $createdAt, " +
        "approved = $approved, location = $location, startTime = $startTime, endTime = $endTime, ignored = $ignored)"
  }
}
