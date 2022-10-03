package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.enums.RoleName
import javax.persistence.*

@Entity
@Table(name = "roles")
class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  var roleId: Int? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "name")
  var roleName: RoleName? = null
  override fun toString(): String {
    return "Role($roleName)"
  }
}
