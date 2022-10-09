package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.enums.RoleName
import javax.persistence.*

@Entity
@Table(name = "users")
class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id") var id: Int? = null

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "managerid") var manager: User? = null

  @Column(name = "username", nullable = false, length = 40) var username: String? = null

  @Column(name = "password", nullable = false, length = 60, columnDefinition = "bpchar")
  var password: String? = null

  @Column(name = "firstname", nullable = false, length = 60) var firstname: String? = null

  @Column(name = "lastname", nullable = false, length = 60) var lastname: String? = null

  @Column(name = "fulltime", nullable = false) var fullTime: Boolean? = null

  @Column(name = "changed") var changed: Boolean? = false

  @Column(name = "points", columnDefinition = "int2") var points: Int? = null

  @OneToMany(mappedBy = "createdUser", fetch = FetchType.LAZY)
  var createdDpms: MutableList<Dpm>? = null

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) var dpms: MutableList<Dpm>? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(
    name = "user_roles",
    joinColumns = [JoinColumn(name = "user_id")],
    inverseJoinColumns = [JoinColumn(name = "role_id")]
  )
  var role: Role? = null

  override fun toString(): String {
    return "User(id=$id, manager='${manager?.firstname} ${manager?.lastname}', username=$username, firstname=$firstname, lastname=$lastname, fullTime=$fullTime, points=$points, role=$role)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (manager != other.manager) return false
    if (username != other.username) return false
    if (password != other.password) return false
    if (firstname != other.firstname) return false
    if (lastname != other.lastname) return false
    if (fullTime != other.fullTime) return false
    if (changed != other.changed) return false
    if (points != other.points) return false
    if (createdDpms != other.createdDpms) return false
    if (dpms != other.dpms) return false
    if (role != other.role) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id ?: 0
    result = 31 * result + (manager?.hashCode() ?: 0)
    result = 31 * result + (username?.hashCode() ?: 0)
    result = 31 * result + (password?.hashCode() ?: 0)
    result = 31 * result + (firstname?.hashCode() ?: 0)
    result = 31 * result + (lastname?.hashCode() ?: 0)
    result = 31 * result + (fullTime?.hashCode() ?: 0)
    result = 31 * result + (changed?.hashCode() ?: 0)
    result = 31 * result + (points ?: 0)
    result = 31 * result + (createdDpms?.hashCode() ?: 0)
    result = 31 * result + (dpms?.hashCode() ?: 0)
    result = 31 * result + (role?.hashCode() ?: 0)
    return result
  }

  fun hasAnyRole(vararg roles: RoleName): Boolean {
    // shadow role to avoid smart cast issues
    val role = role
    if (role?.roleName == null) return false
    return roles.contains(role.roleName)
  }
}
