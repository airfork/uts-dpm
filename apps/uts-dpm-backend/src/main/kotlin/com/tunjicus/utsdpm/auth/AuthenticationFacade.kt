package com.tunjicus.utsdpm.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade : IAuthenticationFacade {
  override fun getAuthentication(): Authentication =
    SecurityContextHolder.getContext().authentication
}
