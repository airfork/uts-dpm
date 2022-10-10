package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
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
    fun from(dpm: Dpm): ApprovalDpmDto {
      return ApprovalDpmDto(
        id = dpm.id!!,
        driver = dpm.user?.firstname!! + " " + dpm.user?.lastname!!,
        createdBy = dpm.createdUser?.firstname!! + " " + dpm.createdUser?.lastname,
        type = dpm.dpmType!!,
        points = dpm.points!!,
        block = dpm.block!!,
        location = dpm.location!!,
        date = FormatHelpers.outboundDpmDate(dpm.date),
        time = FormatHelpers.outboundDpmTime(dpm.startTime, dpm.endTime),
        createdAt = FormatHelpers.createdAt(dpm.created),
        notes = dpm.notes
      )
    }
  }
}
