package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.dtos.CreateUserDto
import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.*
import com.tunjicus.utsdpm.models.PointsBalanceEmail
import com.tunjicus.utsdpm.models.ResetEmail
import com.tunjicus.utsdpm.models.WelcomeEmail
import com.tunjicus.utsdpm.repositories.UserDpmRepository
import com.tunjicus.utsdpm.repositories.RoleRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import java.security.SecureRandom
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
  private val userRepository: UserRepository,
  private val roleRepository: RoleRepository,
  private val userDpmRepository: UserDpmRepository,
  private val authService: AuthService,
  private val passwordEncoder: PasswordEncoder,
  private val emailService: EmailService,
  private val passwordResetTokenService: PasswordResetTokenService,
  private val appProperties: AppProperties
) {

  fun getAllUserNames(): Collection<UsernameDto> {
    return userRepository.findAllSorted().map { it.toUsernameDto() }
  }

  private fun User.toUsernameDto(): UsernameDto {
    val generateName =
      fun(first: String?, last: String?): String {
        return ((first ?: "") + " " + (last ?: "")).trim()
      }

    return UsernameDto(id ?: -1, generateName(firstname, lastname))
  }

  fun findById(id: Int): GetUserDetailDto {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    return GetUserDetailDto.from(user, getManagers().toList())
  }

  @Transactional
  fun updateUser(dto: UserDetailDto, id: Int) {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

    dto.email?.let { user.username = it.trim() }
    dto.firstname?.let { user.firstname = it.trim() }
    dto.lastname?.let { user.lastname = it.trim() }
    dto.points?.let { user.points = it }
    dto.role?.let {
      val role = RoleName.from(it)
      user.role = if (role != null) roleRepository.findByRoleName(role) else null
    }

    // If user is becoming a fulltimer, set their point balance to 0, and ignore unapproved dpms
    dto.fullTime?.let {
      if (it && user.fullTime != true) {
        user.points = 0
        userDpmRepository.ignoreUnapproved(user)
      }
      user.fullTime = it
    }

    dto.managerId?.let { user.manager = getManagerById(it) }

    userRepository.save(user)
  }

  fun getManagers(): Collection<UsernameDto> =
    userRepository.findAllManagers(listOf(RoleName.MANAGER, RoleName.ADMIN)).map { it.toUsernameDto() }

  fun createUser(userDto: CreateUserDto) {
    val manager = getManagerById(userDto.managerId ?: throw ManagerNotFoundException(""))
    if (userRepository.existsByUsername(userDto.email!!)) {
      throw UserAlreadyExistsException()
    }

    val roleName = RoleName.from(userDto.role ?: "")
    val role = if (roleName != null) roleRepository.findByRoleName(roleName) else null
    val password = generateTempPassword()

    val user =
      User().apply {
        username = userDto.email?.trim()
        firstname = userDto.firstname?.trim()
        lastname = userDto.lastname?.trim()
        points = 0
        this.manager = manager
        this.role = role
        fullTime = userDto.fullTime
        this.password = passwordEncoder.encode(password)
      }

    userRepository.save(user)
    sendWelcomeEmail(user, password)
  }

  private fun getManagerById(id: Int): User {
    val manager = userRepository.findById(id).orElseThrow { ManagerNotFoundException(id.toString()) }
    if (!manager.hasAnyRole(RoleName.MANAGER, RoleName.ADMIN)) {
      throw ManagerNotFoundException(id.toString())
    }
    return manager
  }

  // resets points of part-timers to 0
  // ignores all approved DPMs of part-timers
  @Transactional
  fun resetPointBalances() {
    LOGGER.info("Resetting part-timer point balances")
    userRepository.resetPartTimerPoints()
    userDpmRepository.ignorePartTimerDpms()
  }

  @Transactional
  fun deleteUser(id: Int) {
    val deletedUser = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val currentUser = authService.getCurrentUser()
    if (currentUser.id == deletedUser.id) throw SelfDeleteException()

    userDpmRepository.deleteByUser(deletedUser)
    userDpmRepository.changeCreatedUser(currentUser, deletedUser)
    userRepository.changeManager(currentUser, deletedUser)
    userRepository.delete(deletedUser)
  }

  fun sendPointsEmail(id: Int) {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val manager = user.manager!!
    emailService
      .sendPointsEmail(
        user.username!!,
        PointsBalanceEmail(
          user.firstname!!,
          "${manager.firstname!!} ${manager.lastname!!}",
          user.points!!
        )
      )
      .thenRun { LOGGER.info("Points email sent to ${user.username!!}") }
  }

  fun sendPointsEmailAll() = userRepository.findAll().forEach { sendPointsEmail(it.id!!) }

  @Transactional
  fun resetPassword(id: Int) {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    val token = passwordResetTokenService.createToken(user)

    try {
      sendPasswordResetEmail(user, token).join()
    } catch (e: RuntimeException) {
      LOGGER.warn("Failed to send password reset email to ${user.username}", e)
      throw PasswordChangeException("Failed to send password reset email; reset token was not created")
    }
  }

  private fun sendPasswordResetEmail(user: User, token: String) =
    emailService
      .sendResetPasswordEmail(
        user.username!!,
        ResetEmail(user.firstname!!, "${appProperties.baseUrl}/passwordReset?token=$token")
      )
      .thenRun { LOGGER.info("Sent password reset email to ${user.username!!}") }

  private fun sendWelcomeEmail(user: User, password: String) =
    emailService
      .sendWelcomeEmail(
        user.username!!,
        WelcomeEmail(user.firstname!!, password, appProperties.baseUrl)
      )
      .thenRun { LOGGER.info("Sent welcome email to ${user.username!!}") }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserService::class.java)
    private val SECURE_RANDOM = SecureRandom()
    private const val TEMP_PASSWORD_LENGTH = 16
    private const val TEMP_PASSWORD_CHARSET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    private fun generateTempPassword(): String {
      return (1..TEMP_PASSWORD_LENGTH)
          .map { TEMP_PASSWORD_CHARSET[SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARSET.length)] }
          .joinToString("")
    }
  }
}
