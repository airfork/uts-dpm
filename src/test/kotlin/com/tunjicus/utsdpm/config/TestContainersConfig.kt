package com.tunjicus.utsdpm.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

@TestConfiguration
class TestContainersConfig : ApplicationContextInitializer<ConfigurableApplicationContext> {
  companion object {
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")
      .apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        start()
      }

  }

  override fun initialize(applicationContext: ConfigurableApplicationContext) {
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
      applicationContext,
      "spring.datasource.url=${postgres.jdbcUrl}",
      "spring.datasource.username=${postgres.username}",
      "spring.datasource.password=${postgres.password}",
    )
  }
}
