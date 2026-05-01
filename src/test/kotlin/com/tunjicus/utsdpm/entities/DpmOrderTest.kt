package com.tunjicus.utsdpm.entities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DpmOrderTest {
  @Test
  fun `should compare dpm orders by id`() {
    val left = DpmOrder().apply { id = 1 }
    val right = DpmOrder().apply { id = 1 }

    assertThat(left).isEqualTo(right)
  }

  @Test
  fun `should not consider dpm orders with different ids equal`() {
    val left = DpmOrder().apply { id = 1 }
    val right = DpmOrder().apply { id = 2 }

    assertThat(left).isNotEqualTo(right)
  }
}
