package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.AutogenDpmDto
import com.tunjicus.utsdpm.dtos.AutogenWrapperDto
import com.tunjicus.utsdpm.entities.AutoSubmission
import com.tunjicus.utsdpm.exceptions.AutoSubmitAlreadyCalledException
import com.tunjicus.utsdpm.helpers.BlockComparator
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.models.AutogenDpm
import com.tunjicus.utsdpm.models.Shift
import com.tunjicus.utsdpm.repositories.AutoSubmissionRepository
import com.tunjicus.utsdpm.repositories.W2WColorRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CopyOnWriteArrayList

@Service
class AutogenService(
    private val autoSubmissionRepository: AutoSubmissionRepository,
    private val userDpmService: UserDpmService,
    private val authService: AuthService,
    private val w2wColorRepository: W2WColorRepository,
    private val shiftProvider: ShiftProvider
) {
  fun autogenDtos(): AutogenWrapperDto {
    LOGGER.debug("autogenDtos() called - checking if already submitted today")

    if (!alreadyCalledToday()) {
      LOGGER.debug("Not yet submitted today - generating new autogen DPMs")
      val dpms = autogen().map(AutogenDpmDto::from)
      LOGGER.debug("Generated {} autogen DPM DTOs", dpms.size)
      return AutogenWrapperDto(dpms = dpms)
    }

    LOGGER.debug("Already submitted today - returning cached/regenerated DPMs")
    // handle case where app restarts and DPMs have already been submitted
    if (autogenDpms.isEmpty()) {
      LOGGER.debug("Cache is empty after restart - regenerating DPMs")
      autogenDpms.addAll(autogen().map(AutogenDpmDto::from))
    }
    return AutogenWrapperDto(
        FormatHelpers.submittedAt(
            lastSubmission().submitted.withZoneSameInstant(TimeService.ZONE_ID)),
        autogenDpms)
  }

  @Transactional
  fun autoSubmit() {
    LOGGER.debug("autoSubmit() called - checking if already submitted today")

    if (alreadyCalledToday()) {
      LOGGER.warn("autoSubmit() called but already submitted today - throwing exception")
      throw AutoSubmitAlreadyCalledException()
    }

    val dpms = autogen()
    LOGGER.info("Submitting {} autogen DPMs", dpms.size)

    val currentUser = authService.getCurrentUser()
    LOGGER.debug("Current user for submission: {}", currentUser.username)

    var successCount = 0
    var failCount = 0
    for (dpm in dpms) {
      try {
        LOGGER.debug("Creating DPM for user: {} {}, type: {}, block: {}",
            dpm.name.split(" ").firstOrNull(), dpm.name.split(" ").lastOrNull(),
            dpm.type.dpmName, dpm.block)
        userDpmService.newDpm(dpm, currentUser)
        successCount++
      } catch (e: Exception) {
        failCount++
        LOGGER.warn("Exception creating autogen dpm for {}: {}", dpm.name, e.message, e)
      }
    }

    LOGGER.info("AutoSubmit complete: {} succeeded, {} failed out of {} total",
        successCount, failCount, dpms.size)

    autogenDpms.clear()
    autogenDpms.addAll(dpms.map(AutogenDpmDto::from))
    autoSubmissionRepository.save(AutoSubmission())
    LOGGER.debug("AutoSubmission record saved")
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
    LOGGER.debug("Building DPM color map from active W2W colors")
    val dpmColorMap =
        w2wColorRepository
            .findAllActiveWithDpms()
            .associate {
              it.colorCode to
                  it.dpms?.filter { dpm -> dpm.active }?.maxByOrNull { dpm -> dpm.updatedAt }
            }
            .filterValues { it != null }
            .mapValues { it.value!! }

    LOGGER.debug("DPM color map built with {} entries: {}",
        dpmColorMap.size, dpmColorMap.keys)

    LOGGER.debug("Fetching assigned shifts from provider")
    val allShifts = getAssignedShifts()
    LOGGER.debug("Retrieved {} total shifts", allShifts.size)

    val filteredByColor = allShifts.filter { it.colorId in dpmColorMap }
    LOGGER.debug("After color filter: {} shifts (filtered {} without matching color)",
        filteredByColor.size, allShifts.size - filteredByColor.size)

    val filteredByBlock = filteredByColor.filter { it.block.startsWith("[") }
    LOGGER.debug("After block filter: {} shifts (filtered {} without '[' prefix)",
        filteredByBlock.size, filteredByColor.size - filteredByBlock.size)

    val filteredByPublished = filteredByBlock.filter { "Y".equals(it.published, true) }
    LOGGER.debug("After published filter: {} shifts (filtered {} not published)",
        filteredByPublished.size, filteredByBlock.size - filteredByPublished.size)

    val result = filteredByPublished
        .mapNotNull { AutogenDpm.from(it, dpmColorMap) }
        .sortedWith(BlockComparator())

    LOGGER.debug("Final autogen result: {} DPMs after sorting", result.size)
    return result
  }

  fun getAssignedShifts(): List<Shift> {
    return shiftProvider.getAssignedShifts()
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AutogenService::class.java)
    private val autogenDpms = CopyOnWriteArrayList<AutogenDpmDto>()
  }
}
