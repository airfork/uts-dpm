package com.tunjicus.utsdpm

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry

@OpenAPIDefinition(
  info = Info(
    title = "UTS DPM Backend",
    version = "0.0.1",
    description = "Backend api for UTS DPM rewrite"
  )
)
@SpringBootApplication
class UtsDpmBackendApplication {
  fun addResourceHandlers(registry: ResourceHandlerRegistry) =
    registry.addResourceHandler("/").addResourceLocations("/index.html")
}

fun main(args: Array<String>) {
  runApplication<UtsDpmBackendApplication>(*args)
}
