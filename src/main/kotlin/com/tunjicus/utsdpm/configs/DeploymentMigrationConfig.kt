package com.tunjicus.utsdpm.configs

import kotlin.system.exitProcess
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class DeploymentMigrationConfig {
  @Bean
  @ConditionalOnProperty(
    name = ["app.deployment-migration"],
    havingValue = PRAD_AUDIT_MIGRATION_ID,
  )
  fun pradAuditDeploymentMigrationRunner(
    jdbcTemplate: JdbcTemplate,
    applicationContext: ConfigurableApplicationContext,
  ): ApplicationRunner =
    ApplicationRunner { _: ApplicationArguments ->
      DeploymentMigrationConfig.logger.info(
        "Running deployment migration {}", PRAD_AUDIT_MIGRATION_ID)

      val sql =
        ClassPathResource("db/deployment/20260501_backend_audit_prad.sql")
          .inputStream
          .bufferedReader()
          .use { it.readText() }

      jdbcTemplate.execute(sql)

      DeploymentMigrationConfig.logger.info(
        "Deployment migration {} completed", PRAD_AUDIT_MIGRATION_ID)
      val exitCode = SpringApplication.exit(applicationContext, { 0 })
      exitProcess(exitCode)
    }

  companion object {
    const val PRAD_AUDIT_MIGRATION_ID = "20260501-backend-audit-prad"
    private val logger = LoggerFactory.getLogger(DeploymentMigrationConfig::class.java)
  }
}
