package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.dtos.PostDpmDto
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.Role
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.enums.RoleName
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UserDpmServiceTest : BaseIntegrationTest() {
  @Autowired private lateinit var userDpmService: UserDpmService
  @MockitoBean private lateinit var authService: AuthService

  @BeforeEach
  fun setUp() {
    val adminRole = roleRepository.save(Role().apply { roleName = RoleName.ADMIN })
    val managerRole = roleRepository.save(Role().apply { roleName = RoleName.MANAGER })

    // Create test users
    val sampleManager =
        User().apply {
          username = "manager@test.com"
          firstname = "Test"
          lastname = "Manager"
          fullTime = true
          password = "<PASSWORD>"
          role = managerRole
        }
    userRepository.save(sampleManager)

    val driver =
        User().apply {
          username = "driver@test.com"
          firstname = "Test"
          lastname = "Driver"
          points = 0
          fullTime = true
          password = "<PASSWORD>"
          manager = manager
        }
    userRepository.save(driver)

    val adminUser =
        User().apply {
          username = "admin@test.com"
          firstname = "Admin"
          lastname = "User"
          fullTime = true
          password = "<PASSWORD>"
          role = adminRole
        }
    userRepository.save(adminUser)

    // Mock the auth service to return our test user
    `when`(authService.getCurrentUser()).thenReturn(adminUser)
  }

  @AfterEach
  fun cleanUp() {
    userDpmRepository.deleteAll()
    userRepository.deleteAll()
  }

  @Test
  @Transactional
  fun `should create new DPM successfully`() {
    val dpmGroup = dpmGroupRepository.save(DpmGroup().apply { groupName = "Test Group" })
    val dpm =
        dpmRepository.save(
            Dpm().apply {
              dpmName = "Test DPM"
              points = 10
              this.dpmGroup = dpmGroup
            })

    val dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    // Given
    val driver = userRepository.findByUsername("driver@test.com")!!
    val admin = userRepository.findByUsername("admin@test.com")!!
    `when`(authService.getCurrentUser()).thenReturn(admin)

    val dpmDto =
        PostDpmDto(
            driver = "${driver.firstname} ${driver.lastname}",
            block = "10",
            date = dateFormat.format(LocalDate.now()),
            type = dpm.id,
            location = "OFF",
            startTime = "1000",
            endTime = "1100",
            notes = "Test comment")

    // When
    userDpmService.newDpm(dpmDto)

    val lastWeek = ZonedDateTime.now().minusWeeks(1)
    val tomorrow = ZonedDateTime.now().plusDays(1)

    // Then
    val savedDpms =
        userDpmRepository.findAllByCreatedAtAfterAndCreatedAtBeforeOrderByCreatedAtDesc(
            lastWeek, tomorrow)
    assertThat(savedDpms).hasSize(1)

    val savedDpm = savedDpms.first()
    assertThat(savedDpm.dpmType).isEqualTo(dpm)
    assertThat(savedDpm.points).isEqualTo(10)
    assertThat(savedDpm.createdUser).isEqualTo(admin)
    assertThat(savedDpm.user).isEqualTo(driver)
  }
}
