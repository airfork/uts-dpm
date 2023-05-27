package com.tunjicus.utsdpm.helpers

import com.tunjicus.utsdpm.services.TimeService
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val INBOUND_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
private val OUTBOUND_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
private val CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy '@' HHmm")
private val CREATED_EXCEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
private val SUBMITTED_AT_FORMAT = DateTimeFormatter.ofPattern("HHmm")

object FormatHelpers {
  fun outboundDpmDate(date: LocalDate?): String = DATE_FORMAT.format(date)

  fun outboundDpmTime(start: LocalTime?, end: LocalTime? = null): String {
    if (end == null) return OUTBOUND_TIME_FORMAT.format(start)

    return "${OUTBOUND_TIME_FORMAT.format(start)} - ${OUTBOUND_TIME_FORMAT.format(end)}"
  }

  fun inboundDpmDate(date: String?): LocalDate = LocalDate.parse(date, DATE_FORMAT)

  fun inboundDpmTime(time: String?): LocalTime = LocalTime.parse(time, INBOUND_TIME_FORMAT)

  fun createdAt(date: ZonedDateTime): String =
    CREATED_AT_FORMAT.format(date.withZoneSameInstant(TimeService.ZONE_ID))

  fun createdAtExcel(date: ZonedDateTime): String =
    CREATED_EXCEL_FORMAT.format(date.withZoneSameInstant(TimeService.ZONE_ID))

  fun dateOrNull(date: String, formatter: DateTimeFormatter): ZonedDateTime? {
    return try {
      LocalDate.parse(date, formatter).atStartOfDay().atZone(TimeService.ZONE_ID)
    } catch (_: DateTimeParseException) {
      null
    }
  }

  fun submittedAt(timestamp: ZonedDateTime): String =
    SUBMITTED_AT_FORMAT.format(timestamp.withZoneSameInstant(TimeService.ZONE_ID))

  fun currentYear(): String = LocalDate.now().year.toString()
}
