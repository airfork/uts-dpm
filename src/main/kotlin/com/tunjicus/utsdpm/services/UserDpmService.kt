package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.dtos.*
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.DpmNotFoundException
import com.tunjicus.utsdpm.exceptions.NameNotFoundException
import com.tunjicus.utsdpm.exceptions.UserNotAuthorizedException
import com.tunjicus.utsdpm.exceptions.UserNotFoundException
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.models.AutogenDpm
import com.tunjicus.utsdpm.models.DpmReceivedEmail
import com.tunjicus.utsdpm.repositories.DpmRepository
import com.tunjicus.utsdpm.repositories.UserDpmRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class UserDpmService(
    private val userRepository: UserRepository,
    private val userDpmRepository: UserDpmRepository,
    private val dpmRepository: DpmRepository,
    private val authService: AuthService,
    private val emailService: EmailService,
    private val appProperties: AppProperties
) {
  fun newDpm(dpmDto: PostDpmDto) {
    val createdBy = authService.getCurrentUser()
    val driver =
        userRepository.findByFullName(dpmDto.driver!!) ?: throw NameNotFoundException(dpmDto.driver)
    val dpm = dpmDto.toDpm()

    val dpmType =
        dpmRepository.findById(dpmDto.type!!).orElseThrow { DpmNotFoundException(dpmDto.type) }

    dpm.user = driver
    dpm.createdUser = createdBy
    dpm.dpmType = dpmType
    dpm.points = dpmType.points

    userDpmRepository.save(dpm)
  }

  @Transactional
  fun newDpm(autogenDpm: AutogenDpm, createdBy: User) {
    val driver =
        userRepository.findByFullName(autogenDpm.name)
            ?: throw NameNotFoundException(autogenDpm.name)
    val dpm = autogenDpm.toDpm()

    dpm.user = driver
    dpm.createdUser = createdBy
    userDpmRepository.save(dpm)
  }

  fun getCurrentDpms(): Collection<HomeDpmDto> {
    val currentUser = authService.getCurrentUser()
    val sixMonthsAgo = TimeService.getTodayZonedDateTime().minusMonths(6)

    return userDpmRepository.getCurrentDpms(currentUser.id!!, sixMonthsAgo).map(HomeDpmDto::from)
  }

  // If manager, get unapproved dpms for managed users
  // Get all if admin
  fun getUnapprovedDpms(page: Int, size: Int): Page<ApprovalDpmDto> {
    val pageRequest = PageRequest.of(maxOf(page, 0), size)
    val currentUser = authService.getCurrentUser()

    return when (currentUser.role?.roleName) {
      RoleName.ADMIN -> userDpmRepository.getUnapprovedDpms(pageRequest).map(ApprovalDpmDto::from)
      RoleName.MANAGER ->
          userDpmRepository
              .getUnapprovedDpms(currentUser.id!!, pageRequest)
              .map(ApprovalDpmDto::from)
      else -> throw UserNotAuthorizedException()
    }
  }

  fun updateDpm(id: Int, dto: PatchDpmDto) {
    val dpm = userDpmRepository.findById(id).orElseThrow { DpmNotFoundException(id) }
    val currentUser = authService.getCurrentUser()

    if (!currentUser.hasAnyRole(RoleName.ADMIN, RoleName.MANAGER)) {
      throw UserNotAuthorizedException()
    }

    if (currentUser.hasAnyRole(RoleName.MANAGER) && currentUser.id != dpm.user?.manager?.id) {
      throw UserNotAuthorizedException()
    }

    // always update points if present
    if (dto.points != null) dpm.points = dto.points

    updateApproved(dto, dpm)
    updateIgnored(dto, dpm)

    userDpmRepository.save(dpm)
  }

  fun getAll(id: Int, page: Int, size: Int): Page<DpmDetailDto> {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val pageNumber = maxOf(page, 0)

    return userDpmRepository
        .findAllByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, size))
        .map(DpmDetailDto::from)
  }

  private fun updateApproved(dto: PatchDpmDto, userDpm: UserDpm) {
    if (dto.approved == null) return

    // DPM approved, update points for user
    // don't allow approved = true and ignored = true to happen at the same time
    if (dto.approved && userDpm.approved != true && userDpm.ignored != true) {
      userDpm.user?.points = userDpm.user?.points?.plus(userDpm.points ?: 0)
      userDpm.approved = true
      sendDpmEmail(userDpm)
    }

    // Just change the value
    else if (!dto.approved) {
      userDpm.approved = false
    }
  }

  private fun sendDpmEmail(userDpm: UserDpm) {
    val user = userDpm.user!!
    val manager = user.manager!!

    emailService
        .sendDpmEmail(
            user.username!!,
            DpmReceivedEmail(
                name = user.firstname!!,
                dpmType = userDpm.dpmType!!.dpmName,
                receivedDate = FormatHelpers.outboundDpmDate(userDpm.date),
                manager = "${manager.firstname!!} ${manager.lastname!!}",
                url = appProperties.baseUrl))
        .thenRun { LOGGER.info("DPM email sent to ${user.username!!}") }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserDpmService::class.java)

    private fun updateIgnored(dto: PatchDpmDto, userDpm: UserDpm) {
      if (dto.ignored == null) return

      // DPM ignored, but previously approved
      // Adjust user's points
      if (dto.ignored && userDpm.ignored != true && userDpm.approved == true) {
        userDpm.ignored = true

        val adjustedPoints = (userDpm.points ?: 0) * -1
        userDpm.user?.points = userDpm.user?.points?.plus(adjustedPoints)
      }

      // else, just change the value
      else {
        userDpm.ignored = dto.ignored
      }
    }
  }
}
