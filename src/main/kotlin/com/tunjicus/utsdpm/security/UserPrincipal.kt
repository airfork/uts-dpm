package com.tunjicus.utsdpm.security

import com.tunjicus.utsdpm.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal
private constructor(user: User, private val authorities: MutableCollection<GrantedAuthority>) :
  UserDetails {
  private var username: String
  private var password: String

  init {
    username = user.username!!
    password = user.password!!
  }

  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

  override fun getPassword(): String = password

  override fun getUsername(): String = username

  override fun isAccountNonExpired(): Boolean = true

  override fun isAccountNonLocked(): Boolean = true

  override fun isCredentialsNonExpired(): Boolean = true

  override fun isEnabled(): Boolean = true

  companion object {
    fun fromUser(user: User): UserPrincipal {
      val role = "ROLE_" + (user.role?.roleName ?: "DRIVER")
      return UserPrincipal(user, mutableListOf(SimpleGrantedAuthority(role)))
    }
  }
}
