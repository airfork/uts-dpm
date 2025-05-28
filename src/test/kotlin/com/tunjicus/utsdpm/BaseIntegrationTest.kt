package com.tunjicus.utsdpm

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.config.TestContainersConfig
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.repositories.*
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [TestContainersConfig::class])
abstract class BaseIntegrationTest {
  @Autowired protected lateinit var entityManager: EntityManager

  @Autowired protected lateinit var dpmRepository: DpmRepository
  @Autowired protected lateinit var dpmGroupRepository: DpmGroupRepository
  @Autowired protected lateinit var roleRepository: RoleRepository
  @Autowired protected lateinit var userRepository: UserRepository
  @Autowired protected lateinit var userDpmRepository: UserDpmRepository
  @Autowired protected lateinit var w2wColorRepository: W2WColorRepository

  @Autowired protected lateinit var objectMapper: ObjectMapper

  @Transactional
  fun createDpm(name: String, points: Int, group: DpmGroup) =
      dpmRepository.save(
          Dpm().apply {
            this.dpmName = name
            this.points = points
            this.dpmGroup = group
          })

  @Transactional
  fun createGroup(name: String) =
      dpmGroupRepository.save(DpmGroup().apply { this.groupName = name })
}
