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
      return HomeDpmDto(
        userDpm.dpmType!!.dpmName,
        userDpm.points!!,
        userDpm.block!!,
        userDpm.location!!,
        FormatHelpers.outboundDpmDate(userDpm.date),
        FormatHelpers.outboundDpmTime(userDpm.startTime, userDpm.endTime),
        userDpm.notes
      )
    }
  }
}
