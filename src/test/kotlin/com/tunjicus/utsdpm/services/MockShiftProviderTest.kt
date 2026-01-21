package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.W2WColor
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class MockShiftProviderTest : BaseIntegrationTest() {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(MockShiftProviderTest::class.java)
  }

  @AfterEach
  @Transactional
  fun cleanUp() {
    entityManager.flush()
    entityManager.clear()
    userDpmRepository.deleteAll()
    dpmRepository.deleteAll()
    dpmGroupRepository.deleteAll()
    userRepository.deleteAll()
    w2wColorRepository.deleteAll()
  }

  @Test
  @Transactional
  fun `should generate mock shifts for all users`() {
    // Create some users
    val user1 = createUser("John", "Doe")
    val user2 = createUser("Jane", "Smith")
    val user3 = createUser("Bob", "Johnson")

    // Create active colors with DPMs
    val group = createGroup("Test Group")
    val color1 = createColor("Color 1", "1")
    val color2 = createColor("Color 2", "2")
    createDpmWithColor("DPM 1", group, color1)
    createDpmWithColor("DPM 2", group, color2)

    entityManager.flush()
    entityManager.clear()

    val provider = MockShiftProvider(userRepository, w2wColorRepository)
    val shifts = provider.getAssignedShifts()

    LOGGER.info("Generated ${shifts.size} shifts for ${3} users")

    assertThat(shifts).hasSize(3)
    assertThat(shifts.map { "${it.firstName} ${it.lastName}" })
        .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Johnson")
    assertThat(shifts).allMatch { it.published == "Y" }
    assertThat(shifts).allMatch { it.block.startsWith("[") }
  }

  @Test
  @Transactional
  fun `should return empty list when no users exist`() {
    // Create colors but no users
    val group = createGroup("Test Group")
    val color = createColor("Color 1", "1")
    createDpmWithColor("DPM 1", group, color)

    entityManager.flush()
    entityManager.clear()

    val provider = MockShiftProvider(userRepository, w2wColorRepository)
    val shifts = provider.getAssignedShifts()

    assertThat(shifts).isEmpty()
  }

  @Test
  @Transactional
  fun `should return empty list when no active colors with DPMs exist`() {
    // Create users but no colors
    createUser("John", "Doe")

    entityManager.flush()
    entityManager.clear()

    val provider = MockShiftProvider(userRepository, w2wColorRepository)
    val shifts = provider.getAssignedShifts()

    assertThat(shifts).isEmpty()
  }

  @Test
  @Transactional
  fun `should cycle through colors for multiple users`() {
    // Create more users than colors
    for (i in 1..5) {
      createUser("User$i", "Test")
    }

    val group = createGroup("Test Group")
    val color1 = createColor("Color 1", "C1")
    val color2 = createColor("Color 2", "C2")
    createDpmWithColor("DPM 1", group, color1)
    createDpmWithColor("DPM 2", group, color2)

    entityManager.flush()
    entityManager.clear()

    val provider = MockShiftProvider(userRepository, w2wColorRepository)
    val shifts = provider.getAssignedShifts()

    assertThat(shifts).hasSize(5)
    // Colors should cycle: C1, C2, C1, C2, C1
    val colorIds = shifts.map { it.colorId }
    assertThat(colorIds).containsOnly("C1", "C2")
  }

  @Test
  @Transactional
  fun `should generate unique block numbers for each shift`() {
    for (i in 1..10) {
      createUser("User$i", "Test")
    }

    val group = createGroup("Test Group")
    val color = createColor("Color 1", "1")
    createDpmWithColor("DPM 1", group, color)

    entityManager.flush()
    entityManager.clear()

    val provider = MockShiftProvider(userRepository, w2wColorRepository)
    val shifts = provider.getAssignedShifts()

    assertThat(shifts).hasSize(10)
    // All shifts should have unique block identifiers based on index
    val blocks = shifts.map { it.block }
    LOGGER.info("Generated blocks: $blocks")
    assertThat(blocks).allMatch { it.startsWith("[EB") && it.endsWith("]") }
  }

  @Transactional
  private fun createUser(firstName: String, lastName: String): User {
    return userRepository.save(
        User().apply {
          this.username = "$firstName.$lastName@test.com".lowercase()
          this.firstname = firstName
          this.lastname = lastName
          this.password = "<PASSWORD>"
          this.fullTime = true
        })
  }

  @Transactional
  private fun createColor(name: String, code: String, active: Boolean = true): W2WColor {
    return w2wColorRepository.save(
        W2WColor().apply {
          this.colorName = name
          this.colorCode = code
          this.active = active
          this.hexCode = "#000000"
        })
  }

  @Transactional
  private fun createDpmWithColor(name: String, group: DpmGroup, color: W2WColor) {
    dpmRepository.save(
        com.tunjicus.utsdpm.entities.Dpm().apply {
          this.dpmName = name
          this.points = 5
          this.dpmGroup = group
          this.w2wColor = color
        })
  }
}
