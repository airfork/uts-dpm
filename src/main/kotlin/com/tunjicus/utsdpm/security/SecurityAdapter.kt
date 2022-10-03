package com.tunjicus.utsdpm.security

import javax.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityAdapter(private val userDetailsService: UserDetailsService) {
  companion object {
    private val NO_AUTH_LIST =
      arrayOf(
        "/api/auth/login**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
      )
  }

  @Bean fun jwtAuthenticationFilter(): JwtAuthenticationFilter = JwtAuthenticationFilter()

  @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .headers()
      .frameOptions()
      .sameOrigin()
      .and()
      .csrf()
      .disable()
      .authorizeRequests()
      .antMatchers(*NO_AUTH_LIST)
      .permitAll()
      .antMatchers(HttpMethod.OPTIONS, "/**")
      .permitAll()
      .antMatchers("/api/**")
      .authenticated()
      .and()
      .exceptionHandling()
      .authenticationEntryPoint { request, response, authException ->
        run {
          response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "UNAUTHORIZED : " + authException.message
          )
        }
      }
      .and()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
      .requiresChannel()
      .requestMatchers({ it.getHeader("X-Forwarded-Proto") != null })
      .requiresSecure()

    return http.build()
  }

  @Bean
  fun authenticationProvider(): DaoAuthenticationProvider {
    return DaoAuthenticationProvider().apply {
      setPasswordEncoder(passwordEncoder())
      setUserDetailsService(userDetailsService)
    }
  }

  @Bean
  fun authenticationManager(
    authenticationConfiguration: AuthenticationConfiguration
  ): AuthenticationManager = authenticationConfiguration.authenticationManager
}
