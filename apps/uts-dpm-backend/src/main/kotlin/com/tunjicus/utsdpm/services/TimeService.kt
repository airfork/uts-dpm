package com.tunjicus.utsdpm.services

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeService {
  companion object {
    val ZONE_ID: ZoneId = ZoneId.of("America/New_York")
    fun getTodayDate(): LocalDate = LocalDate.now(ZONE_ID)
    fun getTodayZonedDateTime(): ZonedDateTime = ZonedDateTime.now(ZONE_ID)
  }
}
