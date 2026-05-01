package com.tunjicus.utsdpm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class UtsDpmBackendApplicationTests : BaseIntegrationTest() {

  @Autowired private lateinit var applicationContext: ApplicationContext

  @Test fun contextLoads() {}

  @Test
  fun `registers bounded task executor for async work`() {
    val taskExecutor = applicationContext.getBean("taskExecutor", ThreadPoolTaskExecutor::class.java)

    assertThat(taskExecutor.corePoolSize).isEqualTo(2)
    assertThat(taskExecutor.maxPoolSize).isEqualTo(5)
    assertThat(taskExecutor.threadNamePrefix).isEqualTo("EmailThread-")
  }
}
