package com.tunjicus.utsdpm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.CreateUserDto
import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.security.JwtProvider
import com.tunjicus.utsdpm.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  @MockitoBean private lateinit var userService: UserService
  @MockitoBean private lateinit var jwtProvider: JwtProvider

  @Test
  @WithMockUser
  fun `should return all user names on GET users names`() {
    val usernames = listOf(UsernameDto(1, "John Doe"), UsernameDto(2, "Jane Smith"))

    whenever(userService.getAllUserNames()).thenReturn(usernames)

    mockMvc
        .perform(get("/api/users/names"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("John Doe"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name").value("Jane Smith"))

    verify(userService).getAllUserNames()
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return user detail on GET users by id as ADMIN`() {
    val userDetail =
        GetUserDetailDto(
            email = "test@example.com",
            firstname = "John",
            lastname = "Doe",
            points = 10,
            manager = "Manager Name",
            role = "DRIVER",
            fullTime = true,
            managers = listOf("Manager Name"))

    whenever(userService.findById(1)).thenReturn(userDetail)

    mockMvc
        .perform(get("/api/users/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.firstname").value("John"))
        .andExpect(jsonPath("$.lastname").value("Doe"))
        .andExpect(jsonPath("$.points").value(10))

    verify(userService).findById(1)
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should update user on PATCH users by id`() {
    val updateDto =
        UserDetailDto().apply {
          email = "updated@example.com"
          firstname = "UpdatedFirst"
          lastname = "UpdatedLast"
          role = "DRIVER"
          manager = "Manager"
          fullTime = true
          points = 20
        }

    mockMvc
        .perform(
            patch("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk)

    verify(userService).updateUser(any(), eq(1))
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should create user on POST users`() {
    val createDto =
        CreateUserDto().apply {
          email = "new@example.com"
          firstname = "New"
          lastname = "User"
          role = "DRIVER"
          manager = "Manager"
          fullTime = true
        }

    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isCreated)

    verify(userService).createUser(any())
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should delete user on DELETE users by id`() {
    mockMvc.perform(delete("/api/users/1")).andExpect(status().isOk)

    verify(userService).deleteUser(1)
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return managers on GET users managers`() {
    val managers = listOf("Manager One", "Manager Two")

    whenever(userService.getManagers()).thenReturn(managers)

    mockMvc
        .perform(get("/api/users/managers"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$[0]").value("Manager One"))
        .andExpect(jsonPath("$[1]").value("Manager Two"))

    verify(userService).getManagers()
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should reset point balances on PATCH users points reset`() {
    mockMvc.perform(patch("/api/users/points/reset")).andExpect(status().isOk)

    verify(userService).resetPointBalances()
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should send points email on GET users by id points`() {
    mockMvc.perform(get("/api/users/1/points")).andExpect(status().isOk)

    verify(userService).sendPointsEmail(1)
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should reset user password on GET users by id reset`() {
    mockMvc.perform(get("/api/users/1/reset")).andExpect(status().isOk)

    verify(userService).resetPassword(1)
  }
}
