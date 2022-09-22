package com.tunjicus.utsdpm.exceptions

class UserRoleNotFoundException(role: String) : RuntimeException("User role not found: $role")
