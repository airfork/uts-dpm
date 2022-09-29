package com.tunjicus.utsdpm.configs

import com.tunjicus.utsdpm.interceptors.RequestLoggingInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class AppConfig(private val requestLoggingInterceptor: RequestLoggingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(requestLoggingInterceptor)
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/**").allowedOrigins("http://localhost:4200")
      .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
      .exposedHeaders("Content-Disposition")
  }
}
