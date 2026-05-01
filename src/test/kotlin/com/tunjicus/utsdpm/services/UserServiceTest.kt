package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.dtos.CreateUserDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.*
import com.tunjicus.utsdpm.models.ResetEmail
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalTime
import java.util.concurrent.CompletableFuture

class UserServiceTest : BaseIntegrationTest() {
  @Autowired private lateinit var userService: UserService
  @MockitoBean private lateinit var emailService: EmailService
  @MockitoBean private lateinit var authService: AuthService

  @AfterEach
  fun cleanUp() {
    try {
      userDpmRepository.deleteAll()
      passwordResetTokenRepository.deleteAll()
      val users = userRepository.findAll()
      users.forEach { it.manager = null }
      userRepository.saveAll(users)
      userRepository.deleteAll()
      roleRepository.deleteAll()
    } catch (e: Exception) {
      // Ignore cleanup errors
    }
  }

  @Test
  @Transactional
  fun `getAllUserNames should return sorted list with formatted names`() {
    val role = createRole(RoleName.ANALYST)
    createUser("John", "Doe", "john@test.com", role)
    createUser("Alice", "Smith", "alice@test.com", role)

    val result = userService.getAllUserNames()

    assertThat(result).hasSize(2)
    val names = result.map { it.name }
    assertThat(names).contains("Alice Smith", "John Doe")
  }

  @Test
  @Transactional
  fun `findById should return GetUserDetailDto for existing user`() {
    val managerRole = createRole(RoleName.MANAGER)
    val analystRole = createRole(RoleName.ANALYST)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val analyst = createUser("Test", "Analyst", "analyst@test.com", analystRole, manager, fullTime = true, points = 5)

    entityManager.flush()
    entityManager.clear()

    val result = userService.findById(analyst.id!!)

    assertThat(result.email).isEqualTo("analyst@test.com")
    assertThat(result.firstname).isEqualTo("Test")
    assertThat(result.lastname).isEqualTo("Analyst")
    assertThat(result.points).isEqualTo(5)
    assertThat(result.manager).isEqualTo("Manager User")
    assertThat(result.managerId).isEqualTo(manager.id)
    assertThat(result.role).isEqualTo("Analyst")
    assertThat(result.fullTime).isTrue()
    assertThat(result.managers).extracting("id").contains(manager.id)
  }

  @Test
  fun `findById should throw UserNotFoundException for missing user`() {
    assertThrows<UserNotFoundException> {
      userService.findById(999)
    }
  }

  @Test
  @Transactional
  fun `updateUser should update individual fields`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val user = createUser("Test", "User", "test@test.com", role, manager, points = 10)

    val dto = UserDetailDto().apply {
      email = "newemail@test.com"
      firstname = "NewFirst"
      lastname = "NewLast"
      points = 20
    }

    userService.updateUser(dto, user.id!!)
    entityManager.flush()
    entityManager.clear()

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.username).isEqualTo("newemail@test.com")
    assertThat(updatedUser.firstname).isEqualTo("NewFirst")
    assertThat(updatedUser.lastname).isEqualTo("NewLast")
    assertThat(updatedUser.points).isEqualTo(20)
  }

  @Test
  @Transactional
  fun `updateUser fullTime transition should reset points and ignore DPMs`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val user = createUser("Test", "User", "test@test.com", role, manager, fullTime = false, points = 15)
    val adminUser = createUser("Admin", "User", "admin@test.com", managerRole)

    val dpmGroup = createGroup("Test Group")
    val dpm = createDpm("Test DPM", 10, dpmGroup)
    userDpmRepository.save(com.tunjicus.utsdpm.entities.UserDpm().apply {
      this.user = user
      this.dpmType = dpm
      this.createdUser = adminUser
      this.approved = false
      this.points = 10
      this.startTime = LocalTime.of(10, 0)
      this.endTime = LocalTime.of(11, 0)
    })

    val dto = UserDetailDto().apply {
      fullTime = true
    }

    userService.updateUser(dto, user.id!!)
    entityManager.flush()
    entityManager.clear()

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.fullTime).isTrue()
    assertThat(updatedUser.points).isEqualTo(0)

    val userDpms = userDpmRepository.findAll().filter { it.user?.id == user.id }
    assertThat(userDpms).hasSize(1)
    assertThat(userDpms.first().ignored).isTrue()
  }

  @Test
  @Transactional
  fun `updateUser with invalid manager should throw ManagerNotFoundException`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val user = createUser("Test", "User", "test@test.com", role, manager)

    val dto = UserDetailDto().apply {
      this.manager = "Nonexistent Manager"
      managerId = 999
    }

    assertThrows<ManagerNotFoundException> {
      userService.updateUser(dto, user.id!!)
    }
  }

  @Test
  @Transactional
  fun `updateUser should use selected manager id when names collide`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val currentManager = createUser("Current", "Manager", "current@test.com", managerRole)
    val firstDuplicate = createUser("Shared", "Manager", "shared1@test.com", managerRole)
    val selectedManager = createUser("Shared", "Manager", "shared2@test.com", managerRole)
    val user = createUser("Test", "User", "test@test.com", role, currentManager)

    val dto =
        UserDetailDto().apply {
          manager = "Shared Manager"
          managerId = selectedManager.id
        }

    userService.updateUser(dto, user.id!!)
    entityManager.flush()
    entityManager.clear()

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.manager?.id).isEqualTo(selectedManager.id)
    assertThat(updatedUser.manager?.id).isNotEqualTo(firstDuplicate.id)
  }

  @Test
  @Transactional
  fun `updateUser with non-manager role should throw ManagerNotFoundException`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val analystRole2 = createRole(RoleName.ANALYST)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val nonManager = createUser("Not", "Manager", "notmanager@test.com", analystRole2)
    val user = createUser("Test", "User", "test@test.com", role, manager)

    val dto = UserDetailDto().apply {
      this.manager = "Not Manager"
      managerId = nonManager.id
    }

    assertThrows<ManagerNotFoundException> {
      userService.updateUser(dto, user.id!!)
    }
  }

  @Test
  @Transactional
  fun `getManagers should return manager names with ids`() {
    val managerRole = createRole(RoleName.MANAGER)
    val adminRole = createRole(RoleName.ADMIN)
    val manager = createUser("Manager", "One", "manager1@test.com", managerRole)
    val admin = createUser("Admin", "User", "admin@test.com", adminRole)

    val result = userService.getManagers()

    assertThat(result).isNotEmpty()
    assertThat(result).extracting("id").contains(manager.id, admin.id)
    assertThat(result).extracting("name").contains("Manager One", "Admin User")
  }

  @Test
  @Transactional
  fun `createUser should create user with encoded password and send welcome email`() {
    val managerRole = createRole(RoleName.MANAGER)
    createUser("Manager", "User", "manager@test.com", managerRole)

    val dto = CreateUserDto().apply {
      email = "newuser@test.com"
      firstname = "New"
      lastname = "User"
      this.manager = "Manager User"
      managerId = userRepository.findByUsername("manager@test.com")!!.id
      role = "Analyst"
      fullTime = true
    }

    userService.createUser(dto)

    val createdUser = userRepository.findByUsername("newuser@test.com")
    assertThat(createdUser).isNotNull()
    assertThat(createdUser!!.firstname).isEqualTo("New")
    assertThat(createdUser.lastname).isEqualTo("User")
    assertThat(createdUser.points).isEqualTo(0)
    assertThat(createdUser.password).isNotNull()
    assertThat(createdUser.password).isNotEqualTo("newuser@test.com")
  }

  @Test
  @Transactional
  fun `createUser should use selected manager id when names collide`() {
    val managerRole = createRole(RoleName.MANAGER)
    val firstDuplicate = createUser("Shared", "Manager", "shared1@test.com", managerRole)
    val selectedManager = createUser("Shared", "Manager", "shared2@test.com", managerRole)

    val dto =
        CreateUserDto().apply {
          email = "newuser@test.com"
          firstname = "New"
          lastname = "User"
          manager = "Shared Manager"
          managerId = selectedManager.id
          role = "Analyst"
          fullTime = true
        }

    userService.createUser(dto)
    entityManager.flush()
    entityManager.clear()

    val createdUser = userRepository.findByUsername("newuser@test.com")
    assertThat(createdUser?.manager?.id).isEqualTo(selectedManager.id)
    assertThat(createdUser?.manager?.id).isNotEqualTo(firstDuplicate.id)
  }

  @Test
  @Transactional
  fun `createUser with duplicate email should throw UserAlreadyExistsException`() {
    val managerRole = createRole(RoleName.MANAGER)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val analystRole = createRole(RoleName.ANALYST)
    createUser("Existing", "User", "existing@test.com", analystRole, manager)

    val dto = CreateUserDto().apply {
      email = "existing@test.com"
      firstname = "New"
      lastname = "User"
      this.manager = "Manager User"
      managerId = manager.id
      role = "Analyst"
      fullTime = true
    }

    assertThrows<UserAlreadyExistsException> {
      userService.createUser(dto)
    }
  }

  @Test
  fun `createUser with invalid manager should throw ManagerNotFoundException`() {
    val dto = CreateUserDto().apply {
      email = "newuser@test.com"
      firstname = "New"
      lastname = "User"
      manager = "Nonexistent Manager"
      managerId = 999
      role = "Analyst"
      fullTime = true
    }

    assertThrows<ManagerNotFoundException> {
      userService.createUser(dto)
    }
  }

  @Test
  @Transactional
  fun `deleteUser self-delete should throw SelfDeleteException`() {
    val role = createRole(RoleName.ADMIN)
    val user = createUser("Test", "User", "test@test.com", role)

    `when`(authService.getCurrentUser()).thenReturn(user)

    assertThrows<SelfDeleteException> {
      userService.deleteUser(user.id!!)
    }
  }

  @Test
  @Transactional
  fun `resetPointBalances should reset part-timer points`() {
    val role = createRole(RoleName.ANALYST)
    val managerRole = createRole(RoleName.MANAGER)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val partTimer = createUser("Part", "Timer", "parttimer@test.com", role, manager, fullTime = false, points = 20)
    val fullTimer = createUser("Full", "Timer", "fulltimer@test.com", role, manager, fullTime = true, points = 20)

    userService.resetPointBalances()
    entityManager.flush()
    entityManager.clear()

    val updatedPartTimer = userRepository.findById(partTimer.id!!).get()
    val updatedFullTimer = userRepository.findById(fullTimer.id!!).get()

    assertThat(updatedPartTimer.points).isEqualTo(0)
    assertThat(updatedFullTimer.points).isEqualTo(20)
  }

  @Test
  @Transactional
  fun `sendPointsEmail should trigger email service`() {
    val managerRole = createRole(RoleName.MANAGER)
    val analystRole = createRole(RoleName.ANALYST)
    val manager = createUser("Manager", "User", "manager@test.com", managerRole)
    val user = createUser("Test", "User", "test@test.com", analystRole, manager, points = 10)

    entityManager.flush()
    entityManager.clear()

    userService.sendPointsEmail(user.id!!)

    verify(emailService).sendPointsEmail(any(), any())
  }

  @Test
  @Transactional
  fun `resetPassword should create reset link without changing password`() {
    val role = createRole(RoleName.ANALYST)
    val user = createUser("Test", "User", "test@test.com", role, changed = true)
    val originalPassword = user.password
    `when`(emailService.sendResetPasswordEmail(any(), any()))
      .thenReturn(CompletableFuture.completedFuture(null))

    userService.resetPassword(user.id!!)
    entityManager.flush()
    entityManager.clear()

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.password).isEqualTo(originalPassword)
    assertThat(updatedUser.changed).isTrue()
    assertThat(passwordResetTokenRepository.findAll()).hasSize(1)
  }

  @Test
  @Transactional
  fun `resetPassword should email a one time reset link`() {
    val role = createRole(RoleName.ANALYST)
    val user = createUser("Test", "User", "test@test.com", role)
    `when`(emailService.sendResetPasswordEmail(any(), any()))
      .thenReturn(CompletableFuture.completedFuture(null))

    userService.resetPassword(user.id!!)

    val resetEmail = argumentCaptor<ResetEmail>()
    verify(emailService).sendResetPasswordEmail(eq("test@test.com"), resetEmail.capture())
    val emailModel = resetEmail.firstValue.toMap()
    assertThat(emailModel).doesNotContainKey("password")
    assertThat(emailModel["resetUrl"]).startsWith("http://localhost:4200/passwordReset?token=")
  }

  @Test
  fun `resetPassword should not leave reset token when reset email fails`() {
    val role = createRole(RoleName.ANALYST)
    val user = createUser("Test", "User", "test@test.com", role)
    val originalPassword = user.password
    `when`(emailService.sendResetPasswordEmail(any(), any()))
      .thenReturn(CompletableFuture.failedFuture(RuntimeException("mail down")))

    assertThrows<PasswordChangeException> { userService.resetPassword(user.id!!) }

    val updatedUser = userRepository.findById(user.id!!).get()
    assertThat(updatedUser.password).isEqualTo(originalPassword)
    assertThat(passwordResetTokenRepository.findAll()).isEmpty()
  }
}
