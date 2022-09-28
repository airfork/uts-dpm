package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.*
import com.tunjicus.utsdpm.exceptions.DpmNotFoundException
import com.tunjicus.utsdpm.exceptions.NameNotFoundException
import com.tunjicus.utsdpm.exceptions.UserNotFoundException
import com.tunjicus.utsdpm.repositories.DpmRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DpmService(
  private val userRepository: UserRepository,
  private val dpmRepository: DpmRepository
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(DpmService::class.java)
    private val VALID_TYPES =
      mapOf(
        Pair("Picked up Block (+1 Point)", 1),
        Pair("Good! (+1 Point)", 1),
        Pair("Voluntary Clinic/Road Test Passed (+2 Points)", 2),
        Pair("200 Hours Safe (+2 Points)", 2),
        Pair("Custom (+5 Points)", 5),
        Pair("1-5 Minutes Late to OFF (-1 Point)", -1),
        Pair("1-5 Minutes Late to BLK (-1 Point)", -1),
        Pair("Missed Email Announcement (-2 Points)", -2),
        Pair("Improper Shutdown (-2 Points)", -2),
        Pair("Off-Route (-2 Points)", -2),
        Pair("6-15 Minutes Late to Blk (-3 Points)", -3),
        Pair("Out of Uniform (-5 Points)", -5),
        Pair("Improper Radio Procedure (-2 Points)", -2),
        Pair("Improper Bus Log (-5 Points)", -5),
        Pair("Timesheet/Improper Book Change (-5 Points)", -5),
        Pair("Custom (-5 Points)", -5),
        Pair("Passenger Inconvenience (-5 Points)", -5),
        Pair("16+ Minutes Late (-5 Points)", -5),
        Pair("Attendance Infraction (-10 Points)", -10),
        Pair("Moving Downed Bus (-10 Points)", -10),
        Pair("Improper 10-50 Procedure (-10 Points)", -10),
        Pair("Failed Ride-Along/Road Test (-10 Points)", -10),
        Pair("Custom (-10 Points)", -10),
        Pair("Failure to Report 10-50 (-15 Points)", -15),
        Pair("Insubordination (-15 Points)", -15),
        Pair("Safety Offense (-15 Points)", -15),
        Pair("Preventable Accident 1, 2 (-15 Points)", -15),
        Pair("Custom (-15 Points)", -15),
        Pair("DNS/Did Not Show (-10 Points)", -10),
        Pair("Preventable Accident 3, 4 (-20 Points)", -20),
      )

    fun isValidType(type: String) = VALID_TYPES.contains(type)
    fun pointsForType(type: String) = VALID_TYPES[type]

    fun stripPointsFromType(type: String): String {
      if (!isValidType(type)) return type

      val pointStart = type.indexOf('(')
      if (pointStart == -1) return type

      return type.substring(0, pointStart).trim()
    }
  }

  fun newDpm(dpmDto: PostDpmDto) {
    // TODO: Fix when auth is implemented
    val createdBy = userRepository.findById(1).orElseThrow()
    val driver =
      userRepository.findByFullName(dpmDto.driver!!)
        ?: throw NameNotFoundException(dpmDto.driver)
    val dpm = dpmDto.toDpm()

    dpm.user = driver
    dpm.createdUser = createdBy
    LOGGER.info("Creating dpm: $dpm")

    dpmRepository.save(dpm)
  }

  fun newDpm(autogenDpm: AutogenDpm) {
    // TODO: Fix when auth is implemented
    val createdBy = userRepository.findById(1).orElseThrow()
    val driver =
      userRepository.findByFullName(autogenDpm.name)
        ?: throw NameNotFoundException(autogenDpm.name)
    val dpm = autogenDpm.toDpm()

    dpm.user = driver
    dpm.createdUser = createdBy
    LOGGER.info("Creating autogen dpm: $dpm")

    dpmRepository.save(dpm)
  }

  fun getCurrentDpms(): Collection<HomeDpmDto> {
    // TODO: Fix when auth is implemented
    val currentUser = userRepository.findById(2).orElseThrow()
    val sixMonthsAgo = LocalDateTime.now().minusMonths(6)

    return dpmRepository.getCurrentDpms(currentUser.id!!, sixMonthsAgo).map(HomeDpmDto::from)
  }

  fun getUnapprovedDpms(): Collection<ApprovalDpmDto> =
    dpmRepository.getUnApprovedDpms().map(ApprovalDpmDto::from)

  fun updateDpm(id: Int, dto: PatchDpmDto) {
    val dpm = dpmRepository.findById(id).orElseThrow { DpmNotFoundException(id) }

    // always just update points
    if (dto.points != null) dpm.points = dto.points

    if (dto.approved != null) {
      // DPM approved, update points for user
      // don't allow approved = true and ignored = true to happen at the same time
      if (dto.approved && dpm.approved != true && dpm.ignored != true) {
        dpm.user?.points = dpm.user?.points?.plus(dpm.points ?: 0)
        dpm.approved = true
      }

      // Just change the value
      else if (!dto.approved) {
        dpm.approved = false
      }
    }

    if (dto.ignored != null) {
      // DPM ignored, but previously approved
      // Adjust user's points
      if (dto.ignored && dpm.ignored != true && dpm.approved == true) {
        dpm.approved = false
        dpm.ignored = true

        val adjustedPoints = (dpm.points ?: 0) * -1
        dpm.user?.points = dpm.user?.points?.plus(adjustedPoints)
      }

      // else, just change the value
      else {
        dpm.ignored = dto.ignored
      }
    }

    dpmRepository.save(dpm)
  }

  fun getAll(id: Int, page: Int, size: Int): Page<DpmDetailDto> {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val pageNumber = maxOf(page, 0)

    return dpmRepository
      .findAllByUserOrderByCreatedDesc(user, PageRequest.of(pageNumber, size))
      .map(DpmDetailDto::from)
  }
}