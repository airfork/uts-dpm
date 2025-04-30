package com.tunjicus.utsdpm

import com.tunjicus.utsdpm.configs.AppProperties
import com.tunjicus.utsdpm.configs.EmailProperties
import com.tunjicus.utsdpm.configs.JwtProperties
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import java.util.concurrent.Executor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@OpenAPIDefinition(
    info =
        Info(
            title = "UTS DPM Backend",
            version = "0.0.1",
            description = "Backend api for UTS DPM rewrite"),
    security = [SecurityRequirement(name = "JWT Auth")])
@SecurityScheme(
    name = "JWT Auth",
    `in` = SecuritySchemeIn.HEADER,
    paramName = "Authorization",
    bearerFormat = "Bearer <token>",
    description =
        "Login as a valid user and copy the token. Put Bearer {token}, (no braces), in box.",
    scheme = "Bearer",
    type = SecuritySchemeType.APIKEY)
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(AppProperties::class, EmailProperties::class, JwtProperties::class)
class UtsDpmBackendApplication

fun main(args: Array<String>) {
  runApplication<UtsDpmBackendApplication>(*args)
}

@Bean
fun taskExecutor(): Executor {
  val executor = ThreadPoolTaskExecutor()
  executor.corePoolSize = 2
  executor.maxPoolSize = 5
  executor.queueCapacity = 500
  executor.setThreadNamePrefix("EmailThread-")
  executor.initialize()
  return executor
}
