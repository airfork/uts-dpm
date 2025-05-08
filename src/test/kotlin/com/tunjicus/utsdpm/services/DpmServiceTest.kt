package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.repositories.DpmGroupRepository
import com.tunjicus.utsdpm.repositories.DpmRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class DpmServiceTest() : BaseIntegrationTest() {
  @Autowired lateinit var dpmService: DpmService
  @Autowired lateinit var dpmRepository: DpmRepository
  @Autowired lateinit var dpmGroupRepository: DpmGroupRepository
  @Autowired lateinit var entityManager: EntityManager

  @Test
  @Transactional
  fun `should create new DPM Group successfully`() {
    val dpmGroup = dpmService.createGroup("Test Group")
    val secondGroup = dpmService.createGroup("Test Group 2")
    assert(dpmGroup.id != null) { "DPM Group ID should not be null" }

    val groups = dpmService.getDpmGroups()
    assert(groups.size == 2) { "There should be 2 groups" }
    assert(groups.contains(dpmGroup)) { "Group list should contain the new group" }
    assert(groups.contains(secondGroup)) { "Group list should contain the second group" }
  }

  @Test
  @Transactional
  fun `should create new DPM successfully`() {
    val dpmGroup = dpmService.createGroup("Test Group")
    val dpm1 = dpmService.createDpm("Test 1", 10, dpmGroup)
    val dpm2 = dpmService.createDpm("Test 2", 10, dpmGroup)

    val dpms = dpmRepository.findAll()
    assert(dpms.size == 2) { "There should be 2 dpms" }
    assert(dpms.contains(dpm1)) { "DPM list should contain the first DPM" }
    assert(dpms.contains(dpm2)) { "DPM list should contain the second DPM" }

    //    val groups = dpmService.getDpmGroups()
    //    assert(groups.size == 1) { "There should be 1 group" }
    //    assert(groups.contains(dpmGroup)) { "Group list should contain the new group" }
    //    assert(dpmGroup.dpms.size == 2) { "DPM Group should have 2 DPMs" }
    //    assert(dpmGroup.dpms.contains(dpm1)) { "DPM Group should contain the first DPM" }
  }

  @Test
  @Transactional
  fun `should have dpms returned in dpm group`() {
    val dpmGroup = dpmService.createGroup("Test Group")
    val groupId = dpmGroup.id!!

    val dpm1 = dpmService.createDpm("Test 1", 10, dpmGroup)
    val dpm2 = dpmService.createDpm("Test 2", 10, dpmGroup)

    // Clear persistence context to ensure fresh data
    entityManager.flush()
    entityManager.clear()

    // Use the explicit fetch method
    val loadedGroup =
        dpmGroupRepository.findById(groupId).orElseThrow {
          AssertionError("Group with id $groupId not found")
        }
    LOGGER.info("Loaded group: $loadedGroup with ${loadedGroup.dpms?.size} DPMs")

    // Verify DPMs are present in the collection
    assert(loadedGroup.dpms!!.any { it.id == dpm1.id }) {
      "DPM Group should contain the first DPM (ID: ${dpm1.id})"
    }

    assert(loadedGroup.dpms!!.any { it.id == dpm2.id }) {
      "DPM Group should contain the second DPM (ID: ${dpm2.id})"
    }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(DpmServiceTest::class.java)
  }
}
