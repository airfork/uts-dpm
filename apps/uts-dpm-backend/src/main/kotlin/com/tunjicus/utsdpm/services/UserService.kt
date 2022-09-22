package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.ManagerNotFoundException
import com.tunjicus.utsdpm.exceptions.UserNotFoundException
import com.tunjicus.utsdpm.repositories.RoleRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
  private val userRepository: UserRepository,
  private val roleRepository: RoleRepository
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserService::class.java)
  }

  fun getAllUserNames(): Collection<UsernameDto> {
    val generateName =
      fun(first: String?, last: String?): String {
        return ((first ?: "") + " " + (last ?: "")).trim()
      }
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
    dto.role?.let { user.role = roleRepository.findByRoleName(RoleName.from(it)) }

    dto.manager?.let {
      val manager = userRepository.findByFullName(it) ?: throw ManagerNotFoundException(it)
      if (manager.role?.roleName != RoleName.MANAGER && manager.role?.roleName != RoleName.ADMIN)
        throw ManagerNotFoundException(it)
      user.manager = manager
    }

    userRepository.save(user)
  }
}
