package com.tunjicus.utsdpm

import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.config.TestContainersConfig
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.Role
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.enums.RoleName
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
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

  @Transactional
  fun createRole(roleName: RoleName) =
      roleRepository.save(Role().apply { this.roleName = roleName })

  @Transactional
  fun createUser(
      firstname: String,
      lastname: String,
      email: String,
      role: Role,
      manager: User? = null,
      fullTime: Boolean = true,
      points: Int = 0,
      changed: Boolean = false
  ) =
      userRepository.save(
          User().apply {
            this.firstname = firstname
            this.lastname = lastname
            this.username = email
            this.password = "password"
            this.role = role
            this.manager = manager
            this.fullTime = fullTime
            this.points = points
            this.changed = changed
          })

  @Transactional
  fun createAuthenticatedMockUser(
      firstname: String,
      lastname: String,
      email: String,
      role: Role
  ): User = createUser(firstname, lastname, email, role)

  @Transactional
  fun createUserDpm(
      user: User,
      createdBy: User,
      dpmType: Dpm,
      points: Int = 10,
      approved: Boolean = false,
      ignored: Boolean = false,
      createdAt: ZonedDateTime? = null
  ) =
      userDpmRepository.save(
          UserDpm().apply {
            this.user = user
            this.createdUser = createdBy
            this.dpmType = dpmType
            this.points = points
            this.approved = approved
            this.ignored = ignored
            this.startTime = LocalTime.of(10, 0)
            this.endTime = LocalTime.of(11, 0)
            this.block = "10"
            this.location = "OFF"
            this.date = LocalDate.now()
            if (createdAt != null) {
              this.createdAt = createdAt
            }
          })
}
