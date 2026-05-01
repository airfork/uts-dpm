package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.security.JwtProvider
import com.tunjicus.utsdpm.services.DataGenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File

@WebMvcTest(DataGenController::class)
class DataGenControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @MockitoBean private lateinit var dataGenService: DataGenService
  @MockitoBean private lateinit var jwtProvider: JwtProvider

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should generate DPM spreadsheet on GET datagen dpms`() {
    val tempFile = createTempSpreadsheet("dpms")

    `when`(dataGenService.generateDpmSpreadSheet(null, null)).thenReturn(tempFile.absolutePath)

    mockMvc
        .perform(get("/api/datagen/dpms"))
        .andExpect(status().isOk)
        .andExpect(
            content()
                .contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(header().string("Content-Disposition", "attachment; filename=DPMs.xlsx"))

    verify(dataGenService).generateDpmSpreadSheet(null, null)
    assertThat(tempFile).doesNotExist()
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should generate DPM spreadsheet with date parameters`() {
    val tempFile = createTempSpreadsheet("dpms")
    val startDate = "2025-01-01"
    val endDate = "2025-01-31"

    `when`(dataGenService.generateDpmSpreadSheet(startDate, endDate)).thenReturn(tempFile.absolutePath)

    mockMvc
        .perform(get("/api/datagen/dpms").param("startDate", startDate).param("endDate", endDate))
        .andExpect(status().isOk)
        .andExpect(
            content()
                .contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

    verify(dataGenService).generateDpmSpreadSheet(startDate, endDate)
    assertThat(tempFile).doesNotExist()
  }

  @Test
  @WithMockUser(roles = ["ADMIN"])
  fun `should generate user spreadsheet on GET datagen users`() {
    val tempFile = createTempSpreadsheet("users")

    `when`(dataGenService.generateUserSpreadSheet()).thenReturn(tempFile.absolutePath)

    mockMvc
        .perform(get("/api/datagen/users"))
        .andExpect(status().isOk)
        .andExpect(
            content()
                .contentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(header().string("Content-Disposition", "attachment; filename=Users.xlsx"))

    verify(dataGenService).generateUserSpreadSheet()
    assertThat(tempFile).doesNotExist()
  }

  private fun createTempSpreadsheet(prefix: String): File {
    val tempFile = File.createTempFile(prefix, ".xlsx")
    tempFile.writeBytes(byteArrayOf(1, 2, 3))
    tempFile.deleteOnExit()
    return tempFile
  }
}
