package com.tunjicus.utsdpm.enums

import com.tunjicus.utsdpm.exceptions.UserRoleNotFoundException

enum class RoleName(val label: String) {
  ADMIN("Admin"),
  ANALYST("Analyst"),
  MANAGER("Manager"),
  SUPERVISOR("Supervisor");

  companion object {
    fun from(role: String): RoleName? =
      when (role.lowercase()) {
        ADMIN.label.lowercase() -> ADMIN
        ANALYST.label.lowercase() -> ANALYST
        MANAGER.label.lowercase() -> MANAGER
        SUPERVISOR.label.lowercase() -> SUPERVISOR
        else -> null
      }
  }
}
