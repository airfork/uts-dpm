package com.tunjicus.utsdpm

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry

@OpenAPIDefinition(
  info =
    Info(
      title = "UTS DPM Backend",
      version = "0.0.1",
      description = "Backend api for UTS DPM rewrite"
    ),
  security = [SecurityRequirement(name = "JWT Auth")]
)
@SecurityScheme(
  name = "JWT Auth",
  `in` = SecuritySchemeIn.HEADER,
  paramName = "Authorization",
  bearerFormat = "Bearer <token>",
  description =
    "Login as a valid user and copy the token. Put Bearer {token}, (no braces), in box.",
  scheme = "Bearer",
  type = SecuritySchemeType.APIKEY
)
@SpringBootApplication
@EnableScheduling
class UtsDpmBackendApplication {
  fun addResourceHandlers(registry: ResourceHandlerRegistry) =
    registry.addResourceHandler("/").addResourceLocations("/index.html")
}

fun main(args: Array<String>) {
  runApplication<UtsDpmBackendApplication>(*args)
}
