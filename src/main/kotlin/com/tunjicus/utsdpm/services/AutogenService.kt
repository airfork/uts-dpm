package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.dtos.AutogenDpmDto
import com.tunjicus.utsdpm.dtos.AutogenWrapperDto
import com.tunjicus.utsdpm.entities.AutoSubmission
import com.tunjicus.utsdpm.exceptions.AutoSubmitAlreadyCalledException
import com.tunjicus.utsdpm.exceptions.AutogenException
import com.tunjicus.utsdpm.helpers.BlockComparator
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.models.AssignedShifts
import com.tunjicus.utsdpm.models.AutogenDpm
import com.tunjicus.utsdpm.models.Shift
import com.tunjicus.utsdpm.repositories.AutoSubmissionRepository
import com.tunjicus.utsdpm.repositories.W2WColorRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.time.format.DateTimeFormatter
import java.util.concurrent.CopyOnWriteArrayList

@Service
class AutogenService(
    private val autoSubmissionRepository: AutoSubmissionRepository,
    private val userDpmService: UserDpmService,
    private val authService: AuthService,
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper,
    private val w2wColorRepository: W2WColorRepository
) {
  fun autogenDtos(): AutogenWrapperDto {
    if (!alreadyCalledToday()) {
      return AutogenWrapperDto(dpms = autogen().map(AutogenDpmDto::from))
    }

    // handle case where app restarts and DPMs have already been submitted
    if (autogenDpms.isEmpty()) autogenDpms.addAll(autogen().map(AutogenDpmDto::from))
    return AutogenWrapperDto(
        FormatHelpers.submittedAt(
            lastSubmission().submitted.withZoneSameInstant(TimeService.ZONE_ID)),
        autogenDpms)
  }

  @Transactional
  fun autoSubmit() {
    if (alreadyCalledToday()) {
      throw AutoSubmitAlreadyCalledException()
    }

    val dpms = autogen()
    val currentUser = authService.getCurrentUser()
    for (dpm in dpms) {
      try {
        userDpmService.newDpm(dpm, currentUser)
      } catch (e: Exception) {
        LOGGER.warn("Exception creating autogen dpm", e)
      }
    }

    autogenDpms.clear()
    autogenDpms.addAll(dpms.map(AutogenDpmDto::from))
    autoSubmissionRepository.save(AutoSubmission())
  }

  // Run daily
  // Delay one hour so repeated starts don't trigger event
  @Scheduled(fixedRate = 1000 * 60 * 60 * 24, initialDelay = 1000 * 60 * 60)
  @Transactional
  fun cleanupAutoSubmissionsTable() {
    LOGGER.info("Running auto submissions cleanup job")
    val monthAgo = TimeService.getTodayZonedDateTime().minusMonths(1)
    val rowsDeleted = autoSubmissionRepository.deleteBySubmittedBefore(monthAgo)

    LOGGER.info("Cleanup job complete - $rowsDeleted entries removed")
  }

  private fun alreadyCalledToday(): Boolean =
      TimeService.getTodayDate()
          .isEqual(
              lastSubmission().submitted.withZoneSameInstant(TimeService.ZONE_ID).toLocalDate())

  private fun lastSubmission(): AutoSubmission =
      autoSubmissionRepository.findMostRecent() ?: AutoSubmission.min()

  private fun autogen(): List<AutogenDpm> {
    val dpmColorMap =
        w2wColorRepository
            .findAllActiveWithDpms()
            .associate {
              it.colorCode to
                  it.dpms?.filter { dpm -> dpm.active }?.maxByOrNull { dpm -> dpm.updatedAt }
            }
            .filterValues { it != null }
            .mapValues { it.value!! }

    return getAssignedShifts()
        .filter { it.colorId in dpmColorMap }
        .filter { it.block.startsWith("[") }
        .filter { "Y".equals(it.published, true) }
        .mapNotNull { AutogenDpm.from(it, dpmColorMap) }
        .sortedWith(BlockComparator())
  }

  fun getAssignedShifts(): List<Shift> {
    val today = DATE_FORMATTER.format(TimeService.getTodayDate())
    val url =
        UriComponentsBuilder.fromUriString(ASSIGNED_SHIFT_URL)
            .queryParam("start_date", today)
            .queryParam("end_date", today)
            .queryParam("key", appProperties.w2wKey)
            .toUriString()

    val request = Request.Builder().url(url).build()
    CLIENT.newCall(request).execute().use {
      if (!it.isSuccessful) {
        throw AutogenException("When2Work assigned shift request failed with code ${it.code}")
      }

      return objectMapper.readValue(it.body?.string(), AssignedShifts::class.java)?.shifts
          ?: emptyList()
    }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AutogenService::class.java)
    private val CLIENT = OkHttpClient()
    private const val ASSIGNED_SHIFT_URL =
        "https://www7.whentowork.com/cgi-bin/w2wG.dll/api/AssignedShiftList"
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private val autogenDpms = CopyOnWriteArrayList<AutogenDpmDto>()
  }
}
