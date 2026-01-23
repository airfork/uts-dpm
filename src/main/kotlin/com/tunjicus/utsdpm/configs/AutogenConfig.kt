package com.tunjicus.utsdpm.configs

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.repositories.UserRepository
import com.tunjicus.utsdpm.repositories.W2WColorRepository
import com.tunjicus.utsdpm.services.MockShiftProvider
import com.tunjicus.utsdpm.services.RealShiftProvider
import com.tunjicus.utsdpm.services.ShiftProvider
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AutogenConfig(
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val w2wColorRepository: W2WColorRepository
) {

  @PostConstruct
  fun logMockWarning() {
    if (appProperties.autogenMockEnabled) {
      LOGGER.warn("============================================================")
      LOGGER.warn("AUTOGEN MOCK MODE IS ENABLED")
      LOGGER.warn("When2Work API calls will be bypassed with mock data")
      LOGGER.warn("Mock shifts will be generated using real users from database")
      LOGGER.warn("Set app.autogen-mock-enabled=false to use real API")
      LOGGER.warn("============================================================")
    }
  }

  @Bean
  fun shiftProvider(): ShiftProvider {
    return if (appProperties.autogenMockEnabled) {
      LOGGER.info("Creating MockShiftProvider - autogen mock mode enabled")
      MockShiftProvider(userRepository, w2wColorRepository)
    } else {
      LOGGER.info("Creating RealShiftProvider - using When2Work API")
      RealShiftProvider(appProperties, objectMapper)
    }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AutogenConfig::class.java)
  }
}
