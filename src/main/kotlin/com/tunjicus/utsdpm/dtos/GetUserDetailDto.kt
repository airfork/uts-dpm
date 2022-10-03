package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.User

data class GetUserDetailDto(
  val email: String,
  val firstname: String,
  val lastname: String,
  val points: Int,
  val manager: String,
  val role: String,
  val fullTime: Boolean,
  val managers: List<String>
) {
  companion object {
    fun from(user: User, managers: List<String>) =
      GetUserDetailDto(
        email = user.username ?: "",
        firstname = user.firstname ?: "",
        lastname = user.lastname ?: "",
        points = user.points ?: 0,
        manager = ((user.manager?.firstname ?: "") + " " + (user.manager?.lastname ?: "")).trim(),
        role = user.role?.roleName?.label ?: "Driver",
        fullTime = user.fullTime ?: false,
        managers = managers
      )
  }
}
