package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.exceptions.AutogenException
import com.tunjicus.utsdpm.models.AssignedShifts
import com.tunjicus.utsdpm.models.Shift
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder
import java.time.format.DateTimeFormatter

class RealShiftProvider(
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper
) : ShiftProvider {

  override fun getAssignedShifts(): List<Shift> {
    val today = DATE_FORMATTER.format(TimeService.getTodayDate())
    val url =
        UriComponentsBuilder.fromUriString(ASSIGNED_SHIFT_URL)
            .queryParam("start_date", today)
            .queryParam("end_date", today)
            .queryParam("key", appProperties.w2wKey)
            .toUriString()

    LOGGER.debug("Fetching shifts from When2Work API for date: {}", today)
    val request = Request.Builder().url(url).build()
    CLIENT.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        LOGGER.error("When2Work API request failed with code: {}", response.code)
        throw AutogenException("When2Work assigned shift request failed with code ${response.code}")
      }

      val body = response.body?.string()
      val shifts = objectMapper.readValue(body, AssignedShifts::class.java)?.shifts ?: emptyList()
      LOGGER.debug("Retrieved {} shifts from When2Work API", shifts.size)
      return shifts
    }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RealShiftProvider::class.java)
    private val CLIENT = OkHttpClient()
    private const val ASSIGNED_SHIFT_URL =
        "https://www7.whentowork.com/cgi-bin/w2wG.dll/api/AssignedShiftList"
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }
}
