package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.AutogenDpmDto
import com.tunjicus.utsdpm.dtos.AutogenWrapperDto
import com.tunjicus.utsdpm.security.JwtProvider
import com.tunjicus.utsdpm.services.AutogenService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AutogenController::class)
class AutogenControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @MockitoBean private lateinit var autogenService: AutogenService
  @MockitoBean private lateinit var jwtProvider: JwtProvider

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return autogen DTOs on GET autogen`() {
    val autogenDpm =
        AutogenDpmDto(
            name = "John Doe",
            block = "[Block A]",
            startTime = "0800",
            endTime = "1600",
            type = "Late Arrival",
            positive = false)
    val wrapperDto = AutogenWrapperDto(submitted = null, dpms = listOf(autogenDpm))

    `when`(autogenService.autogenDtos()).thenReturn(wrapperDto)

    mockMvc
        .perform(get("/api/autogen"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.dpms").isArray)
        .andExpect(jsonPath("$.dpms[0].name").value("John Doe"))
        .andExpect(jsonPath("$.dpms[0].block").value("[Block A]"))
        .andExpect(jsonPath("$.dpms[0].type").value("Late Arrival"))

    verify(autogenService).autogenDtos()
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should submit autogen DPMs on POST autogen submit`() {
    mockMvc.perform(post("/api/autogen/submit")).andExpect(status().isOk)

    verify(autogenService).autoSubmit()
  }

}
