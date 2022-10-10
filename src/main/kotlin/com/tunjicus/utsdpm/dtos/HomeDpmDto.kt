package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
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
    fun from(dpm: Dpm): HomeDpmDto {
      return HomeDpmDto(
        dpm.dpmType!!,
        dpm.points!!,
        dpm.block!!,
        dpm.location!!,
        FormatHelpers.outboundDpmDate(dpm.date),
        FormatHelpers.outboundDpmTime(dpm.startTime, dpm.endTime),
        dpm.notes
      )
    }
  }
}
