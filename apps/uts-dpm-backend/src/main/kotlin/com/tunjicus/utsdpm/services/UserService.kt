package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.CreateUserDto
import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.ManagerNotFoundException
import com.tunjicus.utsdpm.exceptions.NameNotFoundException
import com.tunjicus.utsdpm.exceptions.UserAlreadyExistsException
import com.tunjicus.utsdpm.exceptions.UserNotFoundException
import com.tunjicus.utsdpm.repositories.RoleRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
  private val userRepository: UserRepository,
  private val roleRepository: RoleRepository,
  private val authService: AuthService,
  private val passwordEncoder: PasswordEncoder
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserService::class.java)

    private fun generateTempPassword(): String {
      val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
      return (1..10).map { charset.random() }.joinToString("")
    }
  }

  fun getAllUserNames(): Collection<UsernameDto> {
    val generateName =
      fun(first: String?, last: String?): String {
        return ((first ?: "") + " " + (last ?: "")).trim()
      }

    LOGGER.info("Current user: ${authService.getCurrentUser()}")
    return userRepository.findAllSorted().map {
      UsernameDto(it.id ?: -1, generateName(it.firstname, it.lastname))
    }
  }

  fun findById(id: Int): GetUserDetailDto {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
    return GetUserDetailDto.from(user, userRepository.findAllManagers().map { it.trim() })
  }

  fun updateUser(dto: UserDetailDto, id: Int) {
    val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }

    dto.email?.let { user.username = it }
    dto.firstname?.let { user.firstname = it }
    dto.lastname?.let { user.lastname = it }
    dto.points?.let { user.points = it }
    dto.fullTime?.let { user.fullTime = it }
    dto.role?.let {
      val role = RoleName.from(it)
      user.role = if (role != null) roleRepository.findByRoleName(role) else null
    }

    dto.manager?.let {
      val manager = userRepository.findByFullName(it) ?: throw ManagerNotFoundException(it)
      if (manager.role?.roleName != RoleName.MANAGER && manager.role?.roleName != RoleName.ADMIN)
        throw ManagerNotFoundException(it)
      user.manager = manager
    }

    userRepository.save(user)
  }

  fun getManagers(): Collection<String> = userRepository.findAllManagers()

  fun createUser(userDto: CreateUserDto) {
    val manager =
      userRepository.findByFullName(userDto.manager ?: "")
        ?: throw NameNotFoundException(userDto.manager ?: "")
    if (userRepository.existsByUsername(userDto.email!!)) {
      throw UserAlreadyExistsException()
    }

    val roleName = RoleName.from(userDto.role ?: "")
    val role = if (roleName != null) roleRepository.findByRoleName(roleName) else null
    val password = generateTempPassword()

    LOGGER.info("Created user with a password of $password")

    val user =
      User().apply {
        username = userDto.email
        firstname = userDto.firstname
        lastname = userDto.lastname
        points = 0
        this.manager = manager
        this.role = role
        fullTime = userDto.fullTime
        this.password = passwordEncoder.encode(password)
      }

    userRepository.save(user)
  }
}
