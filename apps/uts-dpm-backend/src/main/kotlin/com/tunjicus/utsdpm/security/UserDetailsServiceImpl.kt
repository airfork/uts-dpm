package com.tunjicus.utsdpm.security

import com.tunjicus.utsdpm.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {
  override fun loadUserByUsername(username: String?): UserDetails {
    val user =
      userRepository.findByUsername(username ?: "")
        ?: throw UsernameNotFoundException("User not found")
    return UserPrincipal.fromUser(user)
  }
}
