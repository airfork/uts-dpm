package com.tunjicus.utsdpm.security

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter : OncePerRequestFilter() {

  @Autowired private lateinit var jwtProvider: JwtProvider
  @Autowired private lateinit var userDetailsService: UserDetailsService

  companion object {
    private val LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    try {
      val jwt = getJwtFromRequest(request)
      if (jwt != null && jwtProvider.validateToken(jwt)) {
        val username = jwtProvider.getUserUsernameFromJWT(jwt)
        val userDetails = userDetailsService.loadUserByUsername(username)

        val authentication =
          UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
      }
    } catch (ex: Exception) {
      LOGGER.error("Could not set user authentication in security context", ex)
    }

    filterChain.doFilter(request, response)
  }

  private fun getJwtFromRequest(request: HttpServletRequest): String? {
    val bearerToken: String? = request.getHeader("Authorization")
    return if (bearerToken != null && bearerToken.startsWith("Bearer ")) bearerToken.substring(7) else null
  }
}
