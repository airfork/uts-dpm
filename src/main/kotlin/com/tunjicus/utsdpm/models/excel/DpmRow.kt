package com.tunjicus.utsdpm.models.excel

import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.helpers.MiscHelpers

class DpmRow(userDpm: UserDpm) : ExcelRow {
  private val firstName = userDpm.user?.firstname
  private val lastName = userDpm.user?.lastname
  private val block = userDpm.block
  private val location = userDpm.location
  private val startTime = FormatHelpers.outboundDpmTime(userDpm.startTime)
  private val endTime = FormatHelpers.outboundDpmTime(userDpm.endTime)
  private val date = FormatHelpers.outboundDpmDate(userDpm.date)
  private val type = userDpm.dpmType?.dpmName
  private val points = userDpm.points?.toString()
  private val notes = userDpm.notes
  private val status = MiscHelpers.generateDpmStatusMessage(userDpm.approved!!, userDpm.ignored!!)
  private val createdAt = FormatHelpers.createdAtExcel(userDpm.createdAt)
  private val createdBy =
      "${userDpm.createdUser?.firstname} ${userDpm.createdUser?.lastname}".trim()

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
          createdBy)
}
