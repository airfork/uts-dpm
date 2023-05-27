package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@Table(name = "dpms")
class Dpm {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") var id: Int? = null

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "createid")
  var createdUser: User? = null

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "userid")
  var user: User? = null

  @Column(name = "block", length = 20) var block: String? = null

  @Column(name = "date") var date: LocalDate? = null

  @Column(name = "dpmtype") var dpmType: String? = null

  @Column(name = "points", nullable = false, columnDefinition = "int2") var points: Int? = null

  @Column(name = "notes") var notes: String? = null

  @Column(name = "created", updatable = false)
  var created: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "approved") var approved: Boolean? = false

  @Column(name = "location") var location: String? = null

  @Column(name = "starttime", nullable = false) var startTime: LocalTime? = null

  @Column(name = "endtime", nullable = false) var endTime: LocalTime? = null

  @Column(name = "ignored") var ignored: Boolean? = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Dpm

    if (id != other.id) return false
    if (createdUser != other.createdUser) return false
    if (user != other.user) return false
    if (block != other.block) return false
    if (date != other.date) return false
    if (dpmType != other.dpmType) return false
    if (points != other.points) return false
    if (notes != other.notes) return false
    if (created != other.created) return false
    if (approved != other.approved) return false
    if (location != other.location) return false
    if (startTime != other.startTime) return false
    if (endTime != other.endTime) return false
    return ignored == other.ignored
  }

  override fun hashCode(): Int {
    var result = id ?: 0
    result = 31 * result + (createdUser?.hashCode() ?: 0)
    result = 31 * result + (user?.hashCode() ?: 0)
    result = 31 * result + (block?.hashCode() ?: 0)
    result = 31 * result + (date?.hashCode() ?: 0)
    result = 31 * result + (dpmType?.hashCode() ?: 0)
    result = 31 * result + (points ?: 0)
    result = 31 * result + (notes?.hashCode() ?: 0)
    result = 31 * result + created.hashCode()
    result = 31 * result + (approved?.hashCode() ?: 0)
    result = 31 * result + (location?.hashCode() ?: 0)
    result = 31 * result + (startTime?.hashCode() ?: 0)
    result = 31 * result + (endTime?.hashCode() ?: 0)
    result = 31 * result + (ignored?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "Dpm(id=$id, createdUser=${createdUser?.firstname} ${createdUser?.lastname}, user=${user?.firstname} ${user?.lastname}, block=$block, date=$date, dpmType=$dpmType, points=$points, notes=$notes, created=$created, approved=$approved, location=$location, startTime=$startTime, endTime=$endTime, ignored=$ignored)"
  }
}
