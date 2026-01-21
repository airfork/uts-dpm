package com.tunjicus.utsdpm.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.*
import com.tunjicus.utsdpm.security.JwtProvider
import com.tunjicus.utsdpm.services.DpmService
import com.tunjicus.utsdpm.services.UserDpmService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DpmController::class)
class DpmControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  @MockitoBean private lateinit var userDpmService: UserDpmService
  @MockitoBean private lateinit var dpmService: DpmService
  @MockitoBean private lateinit var jwtProvider: JwtProvider

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should create DPM on POST dpms`() {
    val postDto =
        PostDpmDto(
            driver = "John Doe",
            block = "[Block A]",
            date = "01/15/2025",
            type = 1,
            location = "BOS",
            startTime = "0800",
            endTime = "1600",
            notes = "Test note")

    mockMvc
        .perform(
            post("/api/dpms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
        .andExpect(status().isCreated)

    verify(userDpmService).newDpm(postDto)
  }

  @Test
  @WithMockUser
  fun `should return current user's DPMs on GET dpms current`() {
    val homeDpm =
        HomeDpmDto(
            type = "Late Arrival",
            points = 10,
            block = "[Block A]",
            location = "BOS",
            date = "01/15/2025",
            time = "0800-1600",
            notes = "Test note")

    `when`(userDpmService.getCurrentDpms()).thenReturn(listOf(homeDpm))

    mockMvc
        .perform(get("/api/dpms/current"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].type").value("Late Arrival"))
        .andExpect(jsonPath("$[0].points").value(10))
        .andExpect(jsonPath("$[0].block").value("[Block A]"))

    verify(userDpmService).getCurrentDpms()
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should return unapproved DPMs on GET dpms approvals`() {
    val approvalDpm =
        ApprovalDpmDto(
            id = 1,
            driver = "John Doe",
            createdBy = "Admin User",
            type = "Late Arrival",
            points = 10,
            block = "[Block A]",
            location = "BOS",
            date = "01/15/2025",
            time = "0800-1600",
            createdAt = "2025-01-15",
            notes = "Test note")

    val page = PageImpl(listOf(approvalDpm))
    `when`(userDpmService.getUnapprovedDpms(0, 10)).thenReturn(page)

    mockMvc
        .perform(get("/api/dpms/approvals").param("page", "0").param("size", "10"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].driver").value("John Doe"))
        .andExpect(jsonPath("$.content[0].type").value("Late Arrival"))

    verify(userDpmService).getUnapprovedDpms(0, 10)
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should update DPM on PATCH dpms id`() {
    val patchDto = PatchDpmDto(points = 15, approved = true, ignored = false)

    mockMvc
        .perform(
            patch("/api/dpms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto)))
        .andExpect(status().isOk)

    verify(userDpmService).updateDpm(1, patchDto)
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return user DPM history on GET dpms user id`() {
    val detailDpm =
        DpmDetailDto(
            id = 1,
            driver = "John Doe",
            createdBy = "Admin User",
            type = "Late Arrival",
            points = 10,
            block = "[Block A]",
            location = "BOS",
            date = "01/15/2025",
            time = "0800-1600",
            createdAt = "2025-01-15",
            notes = "Test note",
            status = "Approved",
            ignored = false)

    val page = PageImpl(listOf(detailDpm))
    `when`(userDpmService.getAll(1, 0, 10)).thenReturn(page)

    mockMvc
        .perform(get("/api/dpms/user/1").param("page", "0").param("size", "10"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].driver").value("John Doe"))
        .andExpect(jsonPath("$.content[0].status").value("Approved"))

    verify(userDpmService).getAll(1, 0, 10)
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return DPM groups on GET dpms list`() {
    val dpmType = GetDpmTypeDto(id = 1, name = "Late Arrival", points = 10)
    val groupDto = GetDpmGroupDto(groupName = "Attendance", dpms = listOf(dpmType))

    `when`(dpmService.getDpmGroupList()).thenReturn(listOf(groupDto))

    mockMvc
        .perform(get("/api/dpms/list"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].groupName").value("Attendance"))
        .andExpect(jsonPath("$[0].dpms[0].name").value("Late Arrival"))

    verify(dpmService).getDpmGroupList()
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should update DPM groups on PUT dpms list`() {
    val putDpmType = PutDpmTypeDto(dpmType = "Late Arrival", points = 10)
    val putGroupDto = PutDpmGroupDto(groupName = "Attendance", dpms = listOf(putDpmType))

    mockMvc
        .perform(
            put("/api/dpms/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(putGroupDto))))
        .andExpect(status().isOk)

    verify(dpmService).updateDpms(listOf(putGroupDto))
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should return colors on GET dpms colors`() {
    val color = GetW2WColors(colorId = 1, colorName = "Red", hexCode = "#FF0000")

    `when`(dpmService.getColors()).thenReturn(listOf(color))

    mockMvc
        .perform(get("/api/dpms/colors"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].colorId").value(1))
        .andExpect(jsonPath("$[0].colorName").value("Red"))
        .andExpect(jsonPath("$[0].hexCode").value("#FF0000"))

    verify(dpmService).getColors()
  }
}
