package com.tunjicus.utsdpm.entities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {
  @Test
  fun `should compare users by persisted id only`() {
    val left =
      User().apply {
        id = 7
        username = "left"
        points = 1
      }
    val right =
      User().apply {
        id = 7
        username = "right"
        points = 99
      }

    assertThat(left).isEqualTo(right)
  }

  @Test
  fun `should not compare transient users as equal`() {
    val left = User().apply { username = "left" }
    val right = User().apply { username = "left" }

    assertThat(left).isNotEqualTo(right)
  }

  @Test
  fun `should keep hash code stable when mutable fields change`() {
    val user =
      User().apply {
        id = 7
        username = "before"
        points = 1
      }

    val originalHashCode = user.hashCode()

    user.username = "after"
    user.points = 99

    assertThat(user.hashCode()).isEqualTo(originalHashCode)
  }
}
