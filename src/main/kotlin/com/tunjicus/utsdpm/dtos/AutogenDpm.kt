package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.formatInboundDpmTime
import com.tunjicus.utsdpm.models.ShiftInfo
import com.tunjicus.utsdpm.services.TimeService

class AutogenDpm(shiftInfo: ShiftInfo) {
  val name: String = shiftInfo.name
  val block: String = shiftInfo.block
  val startTime: String = shiftInfo.startTime
  val endTime: String = shiftInfo.endTime
  val location: String = shiftInfo.location
  val type: String
  val points: Int
  val notes: String

  init {
    val dpmType = shiftInfo.getDPMType()
    type = dpmType.type
    points = dpmType.points
    notes = dpmType.notes
  }

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
