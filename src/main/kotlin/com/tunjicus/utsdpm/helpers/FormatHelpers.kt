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

  fun inboundDpmDate(date: String?): LocalDate {
    if (date.isNullOrBlank()) return LocalDate.now()
    return LocalDate.parse(date, DATE_FORMAT)
  }

  fun inboundDpmTime(time: String?): LocalTime {
    if (time.isNullOrBlank()) return LocalTime.now()
    return LocalTime.parse(time, INBOUND_TIME_FORMAT)
  }

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

  fun convertW2WTime(timeStr: String): String {
    // Handle empty
    if (timeStr.isBlank()) return ""

    // Convert the input to lowercase for easier handling
    val time = timeStr.lowercase()

    // Extract AM/PM indicator
    val isAm = time.endsWith("am")
    val isPm = time.endsWith("pm")

    // Remove the am/pm suffix
    val timeWithoutAmPm = time.removeSuffix("am").removeSuffix("pm").trim()

    // Split hours and minutes
    val hasColon = timeWithoutAmPm.contains(":")
    val (hoursStr, minutesStr) =
        if (hasColon) {
          timeWithoutAmPm.split(":")
        } else {
          listOf(timeWithoutAmPm, "00")
        }

    // Parse hours and minutes
    var hours = hoursStr.toIntOrNull() ?: 0
    val minutes = minutesStr.toIntOrNull() ?: 0

    // Convert to 24-hour format
    if (isPm && hours < 12) hours += 12
    if (isAm && hours == 12) hours = 0

    // Format the result as HHMM
    return String.format("%02d%02d", hours, minutes)
  }
}
