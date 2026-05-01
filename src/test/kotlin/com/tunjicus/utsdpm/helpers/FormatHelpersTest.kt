package com.tunjicus.utsdpm.helpers

import java.time.LocalDate
import java.time.LocalTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FormatHelpersTest {
  @Test
  fun `should parse inbound DPM date`() {
    assertThat(FormatHelpers.inboundDpmDate("05/01/2026")).isEqualTo(LocalDate.of(2026, 5, 1))
  }

  @Test
  fun `should reject blank inbound DPM date`() {
    assertThrows<IllegalArgumentException> { FormatHelpers.inboundDpmDate("") }
  }

  @Test
  fun `should parse inbound DPM time`() {
    assertThat(FormatHelpers.inboundDpmTime("0915")).isEqualTo(LocalTime.of(9, 15))
  }

  @Test
  fun `should reject blank inbound DPM time`() {
    assertThrows<IllegalArgumentException> { FormatHelpers.inboundDpmTime("") }
  }
}
