package com.tunjicus.utsdpm.configs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.interceptors.RequestLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class AppConfig(
  private val requestLoggingInterceptor: RequestLoggingInterceptor,
  private val environment: Environment
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    if (environment.activeProfiles.contains("local")) {
      registry.addInterceptor(requestLoggingInterceptor)
    }
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    registry
      .addMapping("/**")
      .allowedOriginPatterns("http://localhost:*", "https://utsdpm.com", "https://www.utsdpm.com")
      .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
      .exposedHeaders("Content-Disposition")
  }

  @Bean
  fun getObjectMapper(): ObjectMapper =
    ObjectMapper().apply {
      configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).findAndRegisterModules()
    }
}
