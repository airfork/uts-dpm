package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.helpers.FormatHelpers

open class ApprovalDpmDto(
  val id: Int,
  val driver: String,
  val createdBy: String,
  val type: String,
  val points: Int,
  val block: String,
  val location: String,
  val date: String,
  val time: String,
  val createdAt: String,
  val notes: String?
) {
  companion object {
    fun from(userDpm: UserDpm): ApprovalDpmDto {
      val user = userDpm.required("user", userDpm.user)
      val createdUser = userDpm.required("createdUser", userDpm.createdUser)
      val dpmType = userDpm.required("dpmType", userDpm.dpmType)
      val startTime = userDpm.required("startTime", userDpm.startTime)
      val endTime = userDpm.required("endTime", userDpm.endTime)

      return ApprovalDpmDto(
        id = userDpm.required("id", userDpm.id),
        driver = userDpm.fullName("user", user),
        createdBy = userDpm.fullName("createdUser", createdUser),
        type = dpmType.dpmName,
        points = userDpm.required("points", userDpm.points),
        block = userDpm.required("block", userDpm.block),
        location = userDpm.required("location", userDpm.location),
        date = FormatHelpers.outboundDpmDate(userDpm.required("date", userDpm.date)),
        time = FormatHelpers.outboundDpmTime(startTime, endTime),
        createdAt = FormatHelpers.createdAt(userDpm.createdAt),
        notes = userDpm.notes
      )
    }
  }
}
