package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Role
import com.tunjicus.utsdpm.enums.RoleName
import org.springframework.data.repository.CrudRepository

interface RoleRepository : CrudRepository<Role, Int> {
  fun findByRoleName(name: RoleName): Role?
}
