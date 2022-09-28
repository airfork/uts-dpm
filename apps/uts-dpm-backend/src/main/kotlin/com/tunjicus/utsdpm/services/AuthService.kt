package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.auth.IAuthenticationFacade
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.dtos.LoginResponseDto
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.repositories.UserRepository
import com.tunjicus.utsdpm.security.JwtProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
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
  fun authenticateUser(dto: LoginDto): LoginResponseDto {
    val authentication =
      authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken(dto.username, dto.password)
      )
    SecurityContextHolder.getContext().authentication = authentication

    return LoginResponseDto(jwtProvider.generateToken(authentication))
  }

  fun getCurrentUser(): User {
    val authentication = authenticationFacade.getAuthentication()
    return userRepository.findByUsername(authentication.name)
      ?: throw UsernameNotFoundException("Failed to account for user: ${authentication.name}")
  }

  companion object {
    fun getRole(authorities: Collection<GrantedAuthority>): String {
      if (authorities.isEmpty()) return "DRIVER"
      // prefix is ROLE_ (5 chars)
      return authorities.first().authority.substring(5)
    }
  }
}
