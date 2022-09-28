package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.auth.IAuthenticationFacade
import com.tunjicus.utsdpm.dtos.JwtTokenDto
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.repositories.UserRepository
import com.tunjicus.utsdpm.security.JwtProvider
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthService(
  private val userRepository: UserRepository,
  private val authenticationManager: AuthenticationManager,
  private val jwtProvider: JwtProvider,
  private val authenticationFacade: IAuthenticationFacade
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(AuthService::class.java)
  }

  fun authenticateUser(dto: LoginDto): JwtTokenDto {
    LOGGER.info("Authenticating user")
    val authentication =
      authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken(dto.username, dto.password)
      )
    SecurityContextHolder.getContext().authentication = authentication
    return JwtTokenDto(jwtProvider.generateToken(authentication))
  }

  fun getCurrentUser(): User {
    val authentication = authenticationFacade.getAuthentication()
    return userRepository.findByUsername(authentication.name)
      ?: throw UsernameNotFoundException("Failed to account for user: ${authentication.name}")
  }
}
