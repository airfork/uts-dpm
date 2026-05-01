package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.UserDpm

internal fun <T : Any> UserDpm.required(field: String, value: T?): T =
    value ?: throw IllegalStateException("Cannot map UserDpm ${id ?: "<unsaved>"}: missing $field")

internal fun UserDpm.fullName(field: String, user: User): String {
  val firstName = required("$field.firstname", user.firstname)
  val lastName = required("$field.lastname", user.lastname)

  return "$firstName $lastName"
}
