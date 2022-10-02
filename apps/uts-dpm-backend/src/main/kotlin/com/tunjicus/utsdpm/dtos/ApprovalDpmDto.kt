package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.formatCreatedAt
import com.tunjicus.utsdpm.helpers.formatOutboundDpmDate
import com.tunjicus.utsdpm.helpers.formatOutboundDpmTime

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
        date = formatOutboundDpmDate(dpm.date),
        time = formatOutboundDpmTime(dpm.startTime, dpm.endTime),
        createdAt = formatCreatedAt(dpm.created),
        notes = dpm.notes
      )
    }
  }
}
