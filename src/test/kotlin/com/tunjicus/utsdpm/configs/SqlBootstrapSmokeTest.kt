package com.tunjicus.utsdpm.configs

import java.nio.file.Path
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(
  properties =
    [
      "spring.docker.compose.enabled=false",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.sql.init.mode=never"])
@ContextConfiguration(initializers = [SqlBootstrapSmokeTest.BootstrapDatabaseInitializer::class])
class SqlBootstrapSmokeTest {

  @Autowired private lateinit var jdbcTemplate: JdbcTemplate

  @Test
  fun `compose db scripts bootstrap schema validated by application`() {
    val roleCount = jdbcTemplate.queryForObject("select count(*) from roles", Int::class.java) ?: 0
    val dpmCount = jdbcTemplate.queryForObject("select count(*) from dpms", Int::class.java) ?: 0
    val submittedDateColumnCount =
      jdbcTemplate.queryForObject(
        """
          select count(*)
          from information_schema.columns
          where table_name = 'auto_submissions'
            and column_name = 'submitted_date'
        """
          .trimIndent(),
        Int::class.java) ?: 0

    assertThat(roleCount).isGreaterThanOrEqualTo(4)
    assertThat(dpmCount).isGreaterThan(0)
    assertThat(submittedDateColumnCount).isEqualTo(1)
  }

  @Test
  fun `compose db scripts enforce one active dpm per w2w color`() {
    val existingColorId =
      jdbcTemplate.queryForObject(
        """
          select w2w_color_id
          from dpms
          where active
            and w2w_color_id is not null
          limit 1
        """
          .trimIndent(),
        Int::class.java)

    assertThat(existingColorId).isNotNull

    jdbcTemplate.update(
      """
        insert into dpms (dpm_group_id, name, points, w2w_color_id, active)
        values (1, 'Inactive duplicate color smoke test', 1, ?, false)
      """
        .trimIndent(),
      existingColorId)

    assertThatThrownBy {
        jdbcTemplate.update(
          """
            insert into dpms (dpm_group_id, name, points, w2w_color_id, active)
            values (1, 'Active duplicate color smoke test', 1, ?, true)
          """
            .trimIndent(),
          existingColorId)
      }
      .isInstanceOf(DataIntegrityViolationException::class.java)
  }

  class BootstrapDatabaseInitializer :
    ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
      private val postgres =
        PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
          withDatabaseName("uts_dpm")
          withUsername("postgres")
          withPassword("postgres")
          withFileSystemBind(
            Path.of("").toAbsolutePath().resolve("db_scripts").toString(),
            "/docker-entrypoint-initdb.d",
            BindMode.READ_ONLY)
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
}
