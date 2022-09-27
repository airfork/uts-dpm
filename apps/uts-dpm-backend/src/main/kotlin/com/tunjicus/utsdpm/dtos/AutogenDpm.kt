package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.formatInboundDpmTime
import com.tunjicus.utsdpm.services.TimeService

data class AutogenDpm(
  val name: String,
  val block: String,
  val startTime: String,
  val endTime: String,
  val location: String,
  val type: String,
  val points: Int,
  val notes: String
) {
  fun toDpm(): Dpm {
    val dpm = Dpm()
    dpm.block = block
    dpm.date = TimeService.getTodayDate()
    dpm.dpmType = type
    dpm.location = location
    dpm.notes = notes
    dpm.points = points
    dpm.startTime = formatInboundDpmTime(startTime)
    dpm.endTime = formatInboundDpmTime(endTime)
    return dpm
  }
}
