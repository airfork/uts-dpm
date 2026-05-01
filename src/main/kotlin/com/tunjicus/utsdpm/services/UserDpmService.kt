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
        userRepository
            .findById(dpmDto.driverId!!)
            .orElseThrow { UserNotFoundException(dpmDto.driverId) }
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

  @Transactional
  fun updateDpm(id: Int, dto: PatchDpmDto) {
    val dpm = userDpmRepository.findById(id).orElseThrow { DpmNotFoundException(id) }
    val currentUser = authService.getCurrentUser()

    if (!currentUser.hasAnyRole(RoleName.ADMIN, RoleName.MANAGER)) {
      throw UserNotAuthorizedException()
    }

    if (currentUser.hasAnyRole(RoleName.MANAGER) && currentUser.id != dpm.user?.manager?.id) {
      throw UserNotAuthorizedException()
    }

    val oldContribution = pointsContribution(dpm)
    val wasApproved = dpm.approved == true

    if (dto.points != null) dpm.points = dto.points
    if (dto.approved != null) dpm.approved = dto.approved
    if (dto.ignored != null) dpm.ignored = dto.ignored

    val delta = pointsContribution(dpm) - oldContribution
    if (delta != 0) {
      val user = dpm.user!!
      user.points = (user.points ?: 0) + delta
    }

    userDpmRepository.save(dpm)

    if (!wasApproved && dpm.approved == true && dpm.ignored != true) {
      sendDpmEmail(dpm)
    }
  }

  fun getAll(id: Int, page: Int, size: Int): Page<DpmDetailDto> {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val pageNumber = maxOf(page, 0)

    return userDpmRepository
        .findAllByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, size))
        .map(DpmDetailDto::from)
  }

  private fun pointsContribution(userDpm: UserDpm): Int =
      if (userDpm.approved == true && userDpm.ignored != true) userDpm.points ?: 0 else 0

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
  }
}
