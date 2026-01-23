package com.tunjicus.utsdpm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.ChangePasswordDto
import com.tunjicus.utsdpm.dtos.ChangeRequiredDto
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.dtos.LoginResponseDto
import com.tunjicus.utsdpm.exceptions.UserAuthFailedException
import com.tunjicus.utsdpm.security.JwtProvider
import com.tunjicus.utsdpm.services.AuthService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
class AuthControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  @MockitoBean private lateinit var authService: AuthService
  @MockitoBean private lateinit var jwtProvider: JwtProvider

  @Test
  fun `should return JWT token on successful login`() {
    val loginDto = LoginDto("user@test.com", "password123")
    val responseDto = LoginResponseDto("mock-jwt-token")

    whenever(authService.authenticateUser(any())).thenReturn(responseDto)

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.token").value("mock-jwt-token"))

    verify(authService).authenticateUser(any())
  }

  @Test
  fun `should return 401 on failed login`() {
    val loginDto = LoginDto("user@test.com", "wrongpassword")

    whenever(authService.authenticateUser(any())).thenThrow(UserAuthFailedException())

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
        .andExpect(status().isUnauthorized)
  }

  @Test
  @WithMockUser
  fun `should return ChangeRequiredDto on GET changeCheck`() {
    val changeRequiredDto = ChangeRequiredDto(true)

    whenever(authService.changeRequired()).thenReturn(changeRequiredDto)

    mockMvc
        .perform(get("/api/auth/changeCheck"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.required").value(true))

    verify(authService).changeRequired()
  }

  @Test
  @WithMockUser
  fun `should return 200 on successful password change`() {
    val changePasswordDto =
        ChangePasswordDto().apply {
          currentPassword = "currentPass"
          newPassword = "newPassword123"
          confirmPassword = "newPassword123"
        }

    mockMvc
        .perform(
            patch("/api/auth/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDto)))
        .andExpect(status().isOk)

    verify(authService).changePassword(any())
  }
}
