package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.models.Shift
import com.tunjicus.utsdpm.repositories.UserRepository
import com.tunjicus.utsdpm.repositories.W2WColorRepository
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

class MockShiftProvider(
    private val userRepository: UserRepository,
    private val w2wColorRepository: W2WColorRepository
) : ShiftProvider {

  override fun getAssignedShifts(): List<Shift> {
    LOGGER.debug("Generating mock shifts from database users")

    val users = userRepository.findAllSorted()
    val activeColors = w2wColorRepository.findAllActiveWithDpms()

    if (users.isEmpty()) {
      LOGGER.warn("No users found in database for mock shift generation")
      return emptyList()
    }

    if (activeColors.isEmpty()) {
      LOGGER.warn("No active W2W colors with DPMs found for mock shift generation")
      return emptyList()
    }

    val today = DATE_FORMATTER.format(TimeService.getTodayDate())
    val shifts = mutableListOf<Shift>()

    LOGGER.debug("Generating mock shifts for {} users with {} active colors", users.size, activeColors.size)

    users.forEachIndexed { index, user ->
      val colorIndex = index % activeColors.size
      val color = activeColors.elementAt(colorIndex)
      val blockNumber = (index % 10) + 1
      val startHour = 6 + (index % 12)
      val endHour = startHour + 4

      val shift = Shift(
          published = "Y",
          firstName = user.firstname ?: "Unknown",
          lastName = user.lastname ?: "User",
          startDate = today,
          endDate = today,
          startTime = "${startHour}:00am".let { if (startHour >= 12) "${startHour - 12}:00pm" else it },
          endTime = "${endHour}:00pm".let { if (endHour >= 12) "${endHour - 12}:00pm" else it },
          description = "[${blockNumber}] Mock shift for ${user.firstname} ${user.lastname}",
          colorId = color.colorCode,
          block = "[EB$blockNumber]"
      )
      shifts.add(shift)

      LOGGER.debug("Generated mock shift: user={} {}, colorId={}, block={}",
          user.firstname, user.lastname, color.colorCode, shift.block)
    }

    LOGGER.info("Generated {} mock shifts from {} database users", shifts.size, users.size)
    return shifts
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MockShiftProvider::class.java)
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }
}
