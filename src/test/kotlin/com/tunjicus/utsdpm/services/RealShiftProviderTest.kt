package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.exceptions.AutogenException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RealShiftProviderTest {
  private val provider =
      RealShiftProvider(
          AppProperties(
              baseUrl = "https://example.test",
              w2wUser = "user",
              w2wPass = "pass",
              w2wKey = "key",
              mailgunKey = "mailgun"),
          jacksonObjectMapper())

  @Test
  fun `should parse assigned shifts`() {
    val shifts =
        provider.parseAssignedShifts(
            """
            {
              "AssignedShiftList": [
                {
                  "PUBLISHED": "1",
                  "FIRST_NAME": "Jane",
                  "LAST_NAME": "Driver",
                  "START_DATE": "05/01/2026",
                  "END_DATE": "05/01/2026",
                  "START_TIME": "0900",
                  "END_TIME": "1700",
                  "DESCRIPTION": "[10] Route",
                  "COLOR_ID": "3",
                  "POSITION_NAME": "Driver"
                }
              ]
            }
            """
                .trimIndent(),
            "05/01/2026")

    assertThat(shifts).hasSize(1)
    assertThat(shifts.first().firstName).isEqualTo("Jane")
  }

  @Test
  fun `should reject empty assigned shift responses`() {
    val exception =
        assertThrows<AutogenException> { provider.parseAssignedShifts("", "05/01/2026") }

    assertThat(exception.message).contains("empty").contains("05/01/2026")
  }

  @Test
  fun `should wrap malformed assigned shift responses with request context`() {
    val exception =
        assertThrows<AutogenException> {
          provider.parseAssignedShifts("{not valid json", "05/01/2026")
        }

    assertThat(exception.message).contains("Failed to parse").contains("05/01/2026")
  }
}
