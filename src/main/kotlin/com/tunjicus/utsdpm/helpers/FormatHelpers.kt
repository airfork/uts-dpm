package com.tunjicus.utsdpm.helpers

import com.tunjicus.utsdpm.services.TimeService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val INBOUND_TIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HHmm")
private val OUTBOUND_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
private val CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy '@' HHmm")
private val CREATED_EXCEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
private val SUBMITTED_AT_FORMAT = DateTimeFormatter.ofPattern("HHmm")

fun formatOutboundDpmDate(date: LocalDate?): String = DATE_FORMAT.format(date)

fun formatOutboundDpmTime(start: LocalDateTime?, end: LocalDateTime? = null): String {
  if (end == null) return OUTBOUND_TIME_FORMAT.format(start)

  return "${OUTBOUND_TIME_FORMAT.format(start)} - ${OUTBOUND_TIME_FORMAT.format(end)}"
}

fun formatInboundDpmDate(date: String?): LocalDate = LocalDate.parse(date, DATE_FORMAT)

fun formatInboundDpmTime(time: String?): LocalDateTime {
  val today = DATE_FORMAT.format(LocalDateTime.now())
  return LocalDateTime.parse("$today $time", INBOUND_TIME_FORMAT)
}

fun formatCreatedAt(date: ZonedDateTime): String = CREATED_AT_FORMAT.format(date)

fun formatCreatedAtExcel(date: ZonedDateTime): String = CREATED_EXCEL_FORMAT.format(date)

fun formatDateOrNull(date: String, formatter: DateTimeFormatter): ZonedDateTime? {
  return try {
    LocalDate.parse(date, formatter).atStartOfDay().atZone(TimeService.ZONE_ID)
  } catch (_: DateTimeParseException) {
    null
  }
}

fun formatSubmittedAt(timestamp: ZonedDateTime): String = SUBMITTED_AT_FORMAT.format(timestamp)

fun formatCurrentYear(): String = LocalDate.now().year.toString()
