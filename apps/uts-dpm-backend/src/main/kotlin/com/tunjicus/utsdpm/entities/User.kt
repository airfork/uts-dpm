package com.tunjicus.utsdpm.entities

import javax.persistence.*

@Entity
@Table(name = "users")
class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  var id: Int? = null

  @Column(name = "managerid")
  var managerId: Int? = null

  @Column(name = "username", nullable = false, length = 40)
  var username: String? = null

  @Column(name = "password", nullable = false, length = 60, columnDefinition = "bpchar")
  var password: String? = null

  @Column(name = "firstname", nullable = false, length = 60)
  var firstname: String? = null

  @Column(name = "lastname", nullable = false, length = 60)
  var lastname: String? = null

  @Column(name = "fulltime", nullable = false)
  var fullTime: Boolean? = null

  @Column(name = "changed")
  var changed: Boolean? = null

  @Column(name = "admin")
  var admin: Boolean? = null

  @Column(name = "sup")
  var sup: Boolean? = null

  @Column(name = "analyst")
  var analyst: Boolean? = null

  @Column(name = "points", columnDefinition = "int2")
  var points: Int? = null

  @Column(name = "sessionkey", nullable = false, length = 60)
  var sessionKey: String? = null

  @OneToMany(mappedBy = "createdUser", fetch = FetchType.LAZY)
  var createdDpms: MutableList<Dpm>? = null

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  var dpms: MutableList<Dpm>? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (managerId != other.managerId) return false
    if (username != other.username) return false
    if (password != other.password) return false
    if (firstname != other.firstname) return false
    if (lastname != other.lastname) return false
    if (fullTime != other.fullTime) return false
    if (changed != other.changed) return false
    if (admin != other.admin) return false
    if (sup != other.sup) return false
    if (analyst != other.analyst) return false
    if (points != other.points) return false
    if (sessionKey != other.sessionKey) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id ?: 0
    result = 31 * result + (managerId ?: 0)
    result = 31 * result + (username?.hashCode() ?: 0)
    result = 31 * result + (password?.hashCode() ?: 0)
    result = 31 * result + (firstname?.hashCode() ?: 0)
    result = 31 * result + (lastname?.hashCode() ?: 0)
    result = 31 * result + (fullTime?.hashCode() ?: 0)
    result = 31 * result + (changed?.hashCode() ?: 0)
    result = 31 * result + (admin?.hashCode() ?: 0)
    result = 31 * result + (sup?.hashCode() ?: 0)
    result = 31 * result + (analyst?.hashCode() ?: 0)
    result = 31 * result + (points ?: 0)
    result = 31 * result + (sessionKey?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "User(id=$id, managerId=$managerId, username=$username, password=$password, firstname=$firstname, lastname=$lastname, fullTime=$fullTime, changed=$changed, admin=$admin, sup=$sup, analyst=$analyst, points=$points, sessionKey=$sessionKey)"
  }

}


