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
      return ApprovalDpmDto(
        id = userDpm.id!!,
        driver = userDpm.user?.firstname!! + " " + userDpm.user?.lastname!!,
        createdBy = userDpm.createdUser?.firstname!! + " " + userDpm.createdUser?.lastname,
        type = userDpm.dpmType!!.dpmName,
        points = userDpm.points!!,
        block = userDpm.block!!,
        location = userDpm.location!!,
        date = FormatHelpers.outboundDpmDate(userDpm.date),
        time = FormatHelpers.outboundDpmTime(userDpm.startTime, userDpm.endTime),
        createdAt = FormatHelpers.createdAt(userDpm.created),
        notes = userDpm.notes
      )
    }
  }
}
