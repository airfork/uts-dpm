package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.helpers.FormatHelpers

data class HomeDpmDto(
  val type: String,
  val points: Int,
  val block: String,
  val location: String,
  val date: String,
  val time: String,
  val notes: String?
) {
  companion object {
    fun from(userDpm: UserDpm): HomeDpmDto {
      val dpmType = userDpm.required("dpmType", userDpm.dpmType)
      val startTime = userDpm.required("startTime", userDpm.startTime)
      val endTime = userDpm.required("endTime", userDpm.endTime)

      return HomeDpmDto(
        dpmType.dpmName,
        userDpm.required("points", userDpm.points),
        userDpm.required("block", userDpm.block),
        userDpm.required("location", userDpm.location),
        FormatHelpers.outboundDpmDate(userDpm.required("date", userDpm.date)),
        FormatHelpers.outboundDpmTime(startTime, endTime),
        userDpm.notes
      )
    }
  }
}
