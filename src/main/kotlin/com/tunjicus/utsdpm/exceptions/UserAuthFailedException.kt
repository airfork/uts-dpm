package com.tunjicus.utsdpm.exceptions

import org.springframework.security.core.AuthenticationException

class UserAuthFailedException : AuthenticationException("Failed to authenticate user")
