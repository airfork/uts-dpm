package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.User

data class GetUserDetailDto(
  val email: String,
  val firstname: String,
  val lastname: String,
  val points: Int,
  val manager: String,
  val managerId: Int?,
  val role: String,
  val fullTime: Boolean,
  val managers: List<UsernameDto>
) {
  companion object {
    fun from(user: User, managers: List<UsernameDto>) =
      GetUserDetailDto(
        email = user.username ?: "",
        firstname = user.firstname ?: "",
        lastname = user.lastname ?: "",
        points = user.points ?: 0,
        manager = ((user.manager?.firstname ?: "") + " " + (user.manager?.lastname ?: "")).trim(),
        managerId = user.manager?.id,
        role = user.role?.roleName?.label ?: "Driver",
        fullTime = user.fullTime ?: false,
        managers = managers
      )
  }
}
