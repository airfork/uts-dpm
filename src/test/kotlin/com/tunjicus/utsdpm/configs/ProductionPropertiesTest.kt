package com.tunjicus.utsdpm.configs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.util.Properties

class ProductionPropertiesTest {
  @Test
  fun `should disable springdoc endpoints in production`() {
    val properties =
        Properties().apply {
          ClassPathResource("application-prod.properties").inputStream.use { load(it) }
        }

    assertThat(properties.getProperty("springdoc.api-docs.enabled")).isEqualTo("false")
    assertThat(properties.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("false")
  }
}
