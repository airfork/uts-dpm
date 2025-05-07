package com.tunjicus.utsdpm

import com.tunjicus.utsdpm.config.TestContainersConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [TestContainersConfig::class])
abstract class BaseIntegrationTest {}
