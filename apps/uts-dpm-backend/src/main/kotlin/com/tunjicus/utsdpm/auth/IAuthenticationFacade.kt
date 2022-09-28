package com.tunjicus.utsdpm.auth

import org.springframework.security.core.Authentication

interface IAuthenticationFacade {
  fun getAuthentication(): Authentication
}
