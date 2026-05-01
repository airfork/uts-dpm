package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.enums.RoleName
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy

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
  var createdUserDpms: MutableList<UserDpm>? = null

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) var userDpms: MutableList<UserDpm>? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(
    name = "user_roles",
    joinColumns = [JoinColumn(name = "user_id")],
    inverseJoinColumns = [JoinColumn(name = "role_id")]
  )
  var role: Role? = null

  override fun toString(): String {
    return "User(id=$id, manager='${manager?.firstname} ${manager?.lastname}', username=$username, " +
            "firstname=$firstname, lastname=$lastname, fullTime=$fullTime, points=$points, role=$role)"
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

    other as User

    return id != null && id == other.id
  }

  final override fun hashCode(): Int =
      if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode()
      else javaClass.hashCode()

  fun hasAnyRole(vararg roles: RoleName): Boolean {
    // shadow role to avoid smart cast issues
    val role = role
    if (role?.roleName == null) return false
    return roles.contains(role.roleName)
  }
}
