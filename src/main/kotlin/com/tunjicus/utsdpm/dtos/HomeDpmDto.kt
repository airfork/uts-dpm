package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.formatOutboundDpmDate
import com.tunjicus.utsdpm.helpers.formatOutboundDpmTime

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
    fun from(dpm: Dpm): HomeDpmDto {
      return HomeDpmDto(
        dpm.dpmType!!,
        dpm.points!!,
        dpm.block!!,
        dpm.location!!,
        formatOutboundDpmDate(dpm.date),
        formatOutboundDpmTime(dpm.startTime, dpm.endTime),
        dpm.notes
      )
    }
  }
}
