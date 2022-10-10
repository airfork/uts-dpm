package com.tunjicus.utsdpm.models.excel

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.helpers.MiscHelpers

class DpmRow(dpm: Dpm) : ExcelRow {
  private val firstName = dpm.user?.firstname
  private val lastName = dpm.user?.lastname
  private val block = dpm.block
  private val location = dpm.location
  private val startTime = FormatHelpers.outboundDpmTime(dpm.startTime)
  private val endTime = FormatHelpers.outboundDpmTime(dpm.endTime)
  private val date = FormatHelpers.outboundDpmDate(dpm.date)
  private val type = dpm.dpmType
  private val points = dpm.points?.toString()
  private val notes = dpm.notes
  private val status = MiscHelpers.generateDpmStatusMessage(dpm.approved!!, dpm.ignored!!)
  private val createdAt = FormatHelpers.createdAtExcel(dpm.created)
  private val createdBy = "${dpm.createdUser?.firstname} ${dpm.createdUser?.lastname}".trim()

  override fun getRow(): List<String?> =
    listOf(
      firstName,
      lastName,
      block,
      location,
      startTime,
      endTime,
      date,
      type,
      points,
      notes,
      status,
      createdAt,
      createdBy
    )
}
