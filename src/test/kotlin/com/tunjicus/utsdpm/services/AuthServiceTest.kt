package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.auth.IAuthenticationFacade
import com.tunjicus.utsdpm.dtos.ChangePasswordDto
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.PasswordChangeException
import com.tunjicus.utsdpm.exceptions.UserAuthFailedException
import com.tunjicus.utsdpm.security.JwtProvider
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean

class AuthServiceTest : BaseIntegrationTest() {
  @Autowired private lateinit var authService: AuthService

  @MockitoBean private lateinit var authenticationManager: AuthenticationManager
  @MockitoBean private lateinit var jwtProvider: JwtProvider
  @MockitoBean private lateinit var authenticationFacade: IAuthenticationFacade
  @MockitoBean private lateinit var passwordEncoder: PasswordEncoder

  @Test
  @Transactional
  fun `should return JWT token on successful authentication`() {
    val loginDto = LoginDto("testuser", "password123")
    val mockAuth =
        UsernamePasswordAuthenticationToken(
            "testuser", "password123", listOf(SimpleGrantedAuthority("ROLE_ADMIN")))

    `when`(authenticationManager.authenticate(any())).thenReturn(mockAuth)
    `when`(jwtProvider.generateToken(mockAuth)).thenReturn("mock-jwt-token")

    val result = authService.authenticateUser(loginDto)

    assertThat(result.token).isEqualTo("mock-jwt-token")
    verify(authenticationManager).authenticate(any())
    verify(jwtProvider).generateToken(mockAuth)
  }

  @Test
  fun `should throw UserAuthFailedException on failed authentication`() {
    val loginDto = LoginDto("testuser", "wrongpassword")

    `when`(authenticationManager.authenticate(any()))
        .thenThrow(UserAuthFailedException())

    assertThrows<UserAuthFailedException> { authService.authenticateUser(loginDto) }
  }

  @Test
  @Transactional
  fun `should return user on getCurrentUser with valid authentication`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser@test.com", role)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)

    val result = authService.getCurrentUser()

    assertThat(result.username).isEqualTo("testuser@test.com")
    assertThat(result.firstname).isEqualTo("Test")
    assertThat(result.lastname).isEqualTo("User")
  }

  @Test
  fun `should throw UsernameNotFoundException when no authentication found`() {
    `when`(authenticationFacade.getAuthentication()).thenReturn(null)

    assertThrows<UsernameNotFoundException> { authService.getCurrentUser() }
  }

  @Test
  @Transactional
  fun `should return ChangeRequiredDto with required=true when user changed=false`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser@test.com", role, changed = false)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)

    val result = authService.changeRequired()

    assertThat(result.required).isTrue()
  }

  @Test
  @Transactional
  fun `should return ChangeRequiredDto with required=false when user changed=true`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser2@test.com", role, changed = true)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser2@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)

    val result = authService.changeRequired()

    assertThat(result.required).isFalse()
  }

  @Test
  @Transactional
  fun `should update password and set changed=true on successful password change`() {
    val role = createRole(RoleName.ADMIN)
    val user = createUser("Test", "User", "testuser3@test.com", role, changed = false)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser3@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)
    `when`(passwordEncoder.matches("currentPass", "password")).thenReturn(true)
    `when`(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass")

    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "currentPass"
          newPassword = "newPass123"
          confirmPassword = "newPass123"
        }

    authService.changePassword(changePasswordDto)

    entityManager.flush()
    entityManager.clear()

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.changed).isTrue()
    verify(passwordEncoder).encode("newPass123")
  }

  @Test
  @Transactional
  fun `should throw UserAuthFailedException when current password is wrong`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser4@test.com", role, changed = false)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser4@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)
    `when`(passwordEncoder.matches("wrongPass", "password")).thenReturn(false)

    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "wrongPass"
          newPassword = "newPass123"
          confirmPassword = "newPass123"
        }

    assertThrows<UserAuthFailedException> { authService.changePassword(changePasswordDto) }
  }

  @Test
  @Transactional
  fun `should throw PasswordChangeException when new and confirm passwords do not match`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser5@test.com", role, changed = false)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser5@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)
    `when`(passwordEncoder.matches("currentPass", "password")).thenReturn(true)

    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "currentPass"
          newPassword = "newPass123"
          confirmPassword = "differentPass"
        }

    assertThrows<PasswordChangeException> { authService.changePassword(changePasswordDto) }
  }

  @Test
  @Transactional
  fun `should throw PasswordChangeException when new password matches current password`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser6@test.com", role, changed = false)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser6@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)
    `when`(passwordEncoder.matches("currentPass", "password")).thenReturn(true)

    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "currentPass"
          newPassword = "currentPass"
          confirmPassword = "currentPass"
        }

    assertThrows<PasswordChangeException> { authService.changePassword(changePasswordDto) }
  }

  @Test
  @Transactional
  fun `should throw PasswordChangeException when user changed=true`() {
    val role = createRole(RoleName.ADMIN)
    createUser("Test", "User", "testuser7@test.com", role, changed = true)
    val mockAuth = UsernamePasswordAuthenticationToken("testuser7@test.com", null)

    `when`(authenticationFacade.getAuthentication()).thenReturn(mockAuth)

    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "currentPass"
          newPassword = "newPass123"
          confirmPassword = "newPass123"
        }

    assertThrows<PasswordChangeException> { authService.changePassword(changePasswordDto) }
  }
}
