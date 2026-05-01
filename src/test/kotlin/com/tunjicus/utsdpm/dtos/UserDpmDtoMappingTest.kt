package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.UserDpm
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class UserDpmDtoMappingTest {
  @Test
  fun `approval dto reports missing user with row context`() {
    val thrown = catchThrowable { ApprovalDpmDto.from(UserDpm().apply { id = 123 }) }

    assertThat(thrown)
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot map UserDpm 123: missing user")
  }

  @Test
  fun `home dto reports missing dpm type with row context`() {
    val thrown =
      catchThrowable {
        HomeDpmDto.from(
          UserDpm().apply {
            id = 456
            points = 5
            block = "10"
            location = "CAB"
          })
      }

    assertThat(thrown)
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Cannot map UserDpm 456: missing dpmType")
  }
}
