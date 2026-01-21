package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.dtos.PatchDpmDto
import com.tunjicus.utsdpm.dtos.PostDpmDto
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.Role
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.UserNotAuthorizedException
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CompletableFuture
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UserDpmServiceTest : BaseIntegrationTest() {
  @Autowired private lateinit var userDpmService: UserDpmService
  @MockitoBean private lateinit var authService: AuthService
  @MockitoBean private lateinit var emailService: EmailService

  @BeforeEach
  fun setUp() {
    val adminRole = roleRepository.save(Role().apply { roleName = RoleName.ADMIN })
    val managerRole = roleRepository.save(Role().apply { roleName = RoleName.MANAGER })
    val analystRole = roleRepository.save(Role().apply { roleName = RoleName.ANALYST })

    val sampleManager =
        User().apply {
          username = "manager@test.com"
          firstname = "Test"
          lastname = "Manager"
          fullTime = true
          password = "password"
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
          password = "password"
          manager = sampleManager
          role = analystRole
        }
    userRepository.save(driver)

    val adminUser =
        User().apply {
          username = "admin@test.com"
          firstname = "Admin"
          lastname = "User"
          fullTime = true
          password = "password"
          role = adminRole
        }
    userRepository.save(adminUser)

    whenever(authService.getCurrentUser()).thenReturn(adminUser)
  }

  @AfterEach
  fun cleanUp() {
    userDpmRepository.deleteAll()
    userRepository.deleteAll()
    roleRepository.deleteAll()
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

    val driver = userRepository.findByUsername("driver@test.com")!!
    val admin = userRepository.findByUsername("admin@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(admin)

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

    userDpmService.newDpm(dpmDto)

    val lastWeek = ZonedDateTime.now().minusWeeks(1)
    val tomorrow = ZonedDateTime.now().plusDays(1)

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

  @Test
  @Transactional
  fun `should return only DPMs from last 6 months for getCurrentDpms`() {
    val driver = userRepository.findByUsername("driver@test.com")!!
    val admin = userRepository.findByUsername("admin@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(driver)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    createUserDpm(
        user = driver,
        createdBy = admin,
        dpmType = dpmType,
        points = 10,
        approved = true,
        createdAt = TimeService.getTodayZonedDateTime().minusMonths(5))

    createUserDpm(
        user = driver,
        createdBy = admin,
        dpmType = dpmType,
        points = 10,
        approved = true,
        createdAt = TimeService.getTodayZonedDateTime().minusMonths(7))

    entityManager.flush()
    entityManager.clear()

    val result = userDpmService.getCurrentDpms()

    assertThat(result).hasSize(1)
    assertThat(result.first().points).isEqualTo(10)
  }

  @Test
  @Transactional
  fun `should return all unapproved DPMs for admin`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(admin)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    val manager1 = createUser("Manager", "One", "manager1@test.com", createRole(RoleName.MANAGER))
    val manager2 = createUser("Manager", "Two", "manager2@test.com", createRole(RoleName.MANAGER))
    val driver1 = createUser("Driver", "One", "driver1@test.com", createRole(RoleName.ANALYST), manager = manager1)
    val driver2 = createUser("Driver", "Two", "driver2@test.com", createRole(RoleName.ANALYST), manager = manager2)

    createUserDpm(user = driver1, createdBy = admin, dpmType = dpmType, approved = false)
    createUserDpm(user = driver2, createdBy = admin, dpmType = dpmType, approved = false)

    entityManager.flush()
    entityManager.clear()

    val result = userDpmService.getUnapprovedDpms(0, 10)

    assertThat(result.content).hasSize(2)
  }

  @Test
  @Transactional
  fun `should return only managed users unapproved DPMs for manager`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    val manager = userRepository.findByUsername("manager@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(manager)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    val managedDriver =
        createUser("Managed", "Driver", "managed@test.com", createRole(RoleName.ANALYST), manager = manager)
    val otherManager = createUser("Other", "Manager", "other@test.com", createRole(RoleName.MANAGER))
    val otherDriver =
        createUser("Other", "Driver", "other_driver@test.com", createRole(RoleName.ANALYST), manager = otherManager)

    createUserDpm(user = managedDriver, createdBy = admin, dpmType = dpmType, approved = false)
    createUserDpm(user = otherDriver, createdBy = admin, dpmType = dpmType, approved = false)

    entityManager.flush()
    entityManager.clear()

    val result = userDpmService.getUnapprovedDpms(0, 10)

    assertThat(result.content).hasSize(1)
    assertThat(result.content[0].driver).isEqualTo("Managed Driver")
  }

  @Test
  @Transactional
  fun `should throw UserNotAuthorizedException for driver role`() {
    val driver = userRepository.findByUsername("driver@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(driver)

    assertThrows<UserNotAuthorizedException> { userDpmService.getUnapprovedDpms(0, 10) }
  }

  @Test
  @Transactional
  fun `should add points to user and send email on approval`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    val driver = userRepository.findByUsername("driver@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(admin)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    whenever(emailService.sendDpmEmail(any(), any())).thenReturn(CompletableFuture.completedFuture(null))

    val userDpm = createUserDpm(
        user = driver,
        createdBy = admin,
        dpmType = dpmType,
        points = 10,
        approved = false)

    val initialPoints = driver.points ?: 0
    userDpmService.updateDpm(userDpm.id!!, PatchDpmDto(points = null, approved = true, ignored = null))

    entityManager.flush()
    entityManager.clear()

    val updatedDriver = userRepository.findById(driver.id!!).get()
    assertThat(updatedDriver.points).isEqualTo(initialPoints + 10)
    verify(emailService).sendDpmEmail(any(), any())
  }

  @Test
  @Transactional
  fun `should reverse points when DPM is ignored after approval`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    val driver = userRepository.findByUsername("driver@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(admin)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    val userDpm = createUserDpm(
        user = driver,
        createdBy = admin,
        dpmType = dpmType,
        points = 10,
        approved = true)

    driver.points = (driver.points ?: 0) + 10
    userRepository.save(driver)

    entityManager.flush()
    entityManager.clear()

    val initialPoints = userRepository.findById(driver.id!!).get().points ?: 0

    userDpmService.updateDpm(userDpm.id!!, PatchDpmDto(points = null, approved = null, ignored = true))

    entityManager.flush()
    entityManager.clear()

    val updatedDriver = userRepository.findById(driver.id!!).get()
    assertThat(updatedDriver.points).isEqualTo(initialPoints - 10)
  }

  @Test
  @Transactional
  fun `should throw UserNotAuthorizedException when manager updates non-managed user DPM`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    val manager = userRepository.findByUsername("manager@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(manager)

    val otherManager = createUser("Other", "Manager", "other@test.com", createRole(RoleName.MANAGER))
    val otherDriver =
        createUser("Other", "Driver", "other_driver@test.com", createRole(RoleName.ANALYST), manager = otherManager)

    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    val userDpm = createUserDpm(
        user = otherDriver,
        createdBy = admin,
        dpmType = dpmType,
        approved = false)

    assertThrows<UserNotAuthorizedException> {
      userDpmService.updateDpm(userDpm.id!!, PatchDpmDto(points = null, approved = true, ignored = null))
    }
  }

  @Test
  @Transactional
  fun `should update points value when points are provided`() {
    val admin = userRepository.findByUsername("admin@test.com")!!
    whenever(authService.getCurrentUser()).thenReturn(admin)

    val driver = userRepository.findByUsername("driver@test.com")!!
    val dpmGroup = createGroup("Test Group")
    val dpmType = createDpm("Test DPM", 10, dpmGroup)

    val userDpm = createUserDpm(
        user = driver,
        createdBy = admin,
        dpmType = dpmType,
        points = 10,
        approved = false)

    userDpmService.updateDpm(userDpm.id!!, PatchDpmDto(points = 15, approved = null, ignored = null))

    entityManager.flush()
    entityManager.clear()

    val updatedDpm = userDpmRepository.findById(userDpm.id!!).get()
    assertThat(updatedDpm.points).isEqualTo(15)
  }
}
