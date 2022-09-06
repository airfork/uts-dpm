package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserService::class.java)
  }

  fun getAllUserNames(): Collection<String> = userRepository.getAllNames()
}
