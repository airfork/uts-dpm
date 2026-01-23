package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.auth.IAuthenticationFacade
import com.tunjicus.utsdpm.dtos.ChangePasswordDto
import com.tunjicus.utsdpm.dtos.ChangeRequiredDto
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.dtos.LoginResponseDto
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.exceptions.PasswordChangeException
import com.tunjicus.utsdpm.exceptions.UserAuthFailedException
import com.tunjicus.utsdpm.repositories.UserRepository
import com.tunjicus.utsdpm.security.JwtProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
  private val userRepository: UserRepository,
  private val authenticationManager: AuthenticationManager,
  private val jwtProvider: JwtProvider,
  private val authenticationFacade: IAuthenticationFacade,
  private val passwordEncoder: PasswordEncoder
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
      ?: throw UsernameNotFoundException("No authentication found")
    return userRepository.findByUsername(authentication.name)
      ?: throw UsernameNotFoundException("Failed to account for user: ${authentication.name}")
  }

  fun changeRequired(): ChangeRequiredDto {
    val currentUser = getCurrentUser()
    return ChangeRequiredDto(!currentUser.changed!!)
  }

  fun changePassword(dto: ChangePasswordDto) {
    val currentUser = getCurrentUser()
    if (currentUser.changed != false) {
      throw PasswordChangeException("Password cannot be changed currently")
    }

    if (!passwordEncoder.matches(dto.currentPassword!!, currentUser.password!!)) {
      throw UserAuthFailedException()
    }

    if (dto.newPassword!! != dto.confirmPassword!!) {
      throw PasswordChangeException("Confirm password does not match the new password")
    }

    if (dto.newPassword!! == dto.currentPassword!!) {
      throw PasswordChangeException("New password cannot match the current password")
    }

    currentUser.password = passwordEncoder.encode(dto.newPassword!!)
    currentUser.changed = true
    userRepository.save(currentUser)
  }

  companion object {
    fun getRole(authorities: Collection<GrantedAuthority>): String {
      if (authorities.isEmpty()) return "DRIVER"
      // prefix is ROLE_ (5 chars)
      val authority = authorities.first().authority ?: return "DRIVER"
      return authority.substring(5)
    }
  }
}
