package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.core.type.TypeReference
import com.tunjicus.utsdpm.BaseIntegrationTest
import com.tunjicus.utsdpm.dtos.PutDpmGroupDto
import com.tunjicus.utsdpm.dtos.PutDpmTypeDto
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.models.DpmGroupOrder
import com.tunjicus.utsdpm.repositories.DpmOrderRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class DpmServiceTest() : BaseIntegrationTest() {
  @Autowired lateinit var dpmService: DpmService
  @Autowired lateinit var dpmOrderRepository: DpmOrderRepository
  @Autowired lateinit var entityManager: EntityManager

  @AfterEach
  fun cleanUp() {
    dpmGroupRepository.deleteAll()
    dpmOrderRepository.deleteAll()
    dpmRepository.deleteAll()
  }

  @Test
  @Transactional
  fun `should create new DPM Group successfully`() {
    val dpmGroup = createGroup("Test Group")
    val secondGroup = createGroup("Test Group 2")
    assert(dpmGroup.id != null) { "DPM Group ID should not be null" }

    val groups = dpmGroupRepository.findAllByActiveTrue()
    assert(groups.size == 2) { "There should be 2 groups" }
    assert(groups.contains(dpmGroup)) { "Group list should contain the new group" }
    assert(groups.contains(secondGroup)) { "Group list should contain the second group" }
  }

  @Test
  @Transactional
  fun `should create new DPM successfully`() {
    val dpmGroup = createGroup("Test Group")
    val dpm1 = createDpm("Test 1", 10, dpmGroup)
    val dpm2 = createDpm("Test 2", 10, dpmGroup)

    val dpms = dpmRepository.findAll()
    entityManager.flush()
    entityManager.clear()

    assert(dpms.size == 2) { "There should be 2 dpms" }
    assert(dpms.contains(dpm1)) { "DPM list should contain the first DPM" }
    assert(dpms.contains(dpm2)) { "DPM list should contain the second DPM" }

    val groups = dpmGroupRepository.findAllByActiveTrue()
    assert(groups.size == 1) { "There should be 1 group" }
    assert(groups.contains(dpmGroup)) { "Group list should contain the new group" }
    assert(groups[0].dpms?.size == 2) { "DPM Group should have 2 DPMs" }
    assert(groups[0].dpms?.contains(dpm1) == true) { "DPM Group should contain the first DPM" }
  }

  @Test
  @Transactional
  fun `should have dpms returned in dpm group`() {
    val dpmGroup = createGroup("Test Group")
    val groupId = dpmGroup.id!!

    val dpm1 = createDpm("Test 1", 10, dpmGroup)
    val dpm2 = createDpm("Test 2", 10, dpmGroup)

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

  @Test
  @Transactional
  fun `should create new DPM groups and DPMs via update`() {
    createTestGroupsViaUpdate()
    entityManager.flush()
    entityManager.clear()

    val activeGroups = dpmGroupRepository.findAllByActiveTrue().sortedBy { it.groupName }
    assert(activeGroups.size == 2) { "There should be 2 groups" }
    assert(activeGroups.first().dpms?.size == 2) { "The first group should have 2 DPMs" }
    assert(activeGroups[1].dpms?.size == 1) { "The second group should have 1 DPM" }

    val dpms = dpmRepository.findAllByActiveTrue().sortedBy { it.dpmName }
    assert(dpms.size == 3) { "There should be 3 dpms" }
    assert(dpms.any { it.dpmName == "Test 1" }) { "DPM list should contain the first DPM" }

    assert(dpms.take(2).all { it.dpmGroup == activeGroups.first() }) {
      "The first two DPMs should be in the first group"
    }
    assert(dpms.drop(2).all { it.dpmGroup == activeGroups[1] }) {
      "The remaining DPMs should be in the second group"
    }
  }

  @Test
  @Transactional
  fun `should deactivate groups and dpms after update`() {
    val oldGroup1 = dpmGroupRepository.save(DpmGroup().apply { groupName = "Old Group 1" })
    val oldGroup2 = dpmGroupRepository.save(DpmGroup().apply { groupName = "Old Group 2" })
    val oldDpm1 = createDpm("Old DPM 1", 10, oldGroup1)
    val oldDpm2 = createDpm("Old DPM 2", 10, oldGroup2)

    createTestGroupsViaUpdate()
    entityManager.flush()
    entityManager.clear()

    val inactiveGroups = dpmGroupRepository.findAll().filter { !it.active }
    assert(inactiveGroups.size == 2) { "There should be 2 inactive groups" }
    assert(inactiveGroups.contains(oldGroup1)) { "Inactive group list should contain Old Group 1" }
    assert(inactiveGroups.contains(oldGroup2)) { "Inactive group list should contain Old Group 2" }

    val inactiveDpms = dpmRepository.findAll().filter { !it.active }
    assert(inactiveDpms.size == 2) { "There should be 2 inactive DPMs" }
    assert(inactiveDpms.contains(oldDpm1)) { "Inactive DPM list should contain Old DPM 1" }
    assert(inactiveDpms.contains(oldDpm2)) { "Inactive DPM list should contain Old DPM 2" }
  }

  @Test
  @Transactional
  fun `should not deactivate dpms after update if they have not been modified`() {
    val oldGroup1 = dpmGroupRepository.save(DpmGroup().apply { groupName = "Old Group 1" })
    val persistentDPM = createDpm("Test 2", -10, oldGroup1)
    createTestGroupsViaUpdate()
    entityManager.flush()
    entityManager.clear()

    assert(dpmRepository.findById(persistentDPM.id!!).get().active) {
      "Persistent DPM should be active"
    }
  }

  @Test
  @Transactional
  fun `should have correct ordering in database after dpm update`() {
    createTestGroupsViaUpdate()
    entityManager.flush()
    entityManager.clear()

    val order = dpmOrderRepository.findTopByOrderByUpdatedAtDesc()
    assert(order != null) { "DPM Order should not be null" }

    val typeReference = object : TypeReference<List<DpmGroupOrder>>() {}
    val groupOrder = objectMapper.readValue(order!!.dpmOrder!!, typeReference)
    assert(groupOrder.size == 2) { "There should be 2 groups in the order" }

    val groups = dpmGroupRepository.findAllByActiveTrue().sortedBy { it.groupName }
    assert(groups.size == 2) { "There should be 2 groups in the database" }

    val expectedOrder =
        listOf(
            DpmGroupOrder(groups[0].id!!, groups[0].dpms!!.map { it.id!! }),
            DpmGroupOrder(groups[1].id!!, groups[1].dpms!!.map { it.id!! }))

    assert(groupOrder == expectedOrder) { "DPM Order should be ${expectedOrder.joinToString()}" }
  }

  @Transactional
  fun createTestGroupsViaUpdate() {
    val newDpmTypes =
        listOf(
            PutDpmTypeDto("Test 1", 10),
            PutDpmTypeDto("Test 2", -10),
            PutDpmTypeDto("Test 3", -1),
        )

    val updateGroups =
        listOf(
            PutDpmGroupDto("Test Group 1", newDpmTypes.take(2)),
            PutDpmGroupDto("Test Group 2", newDpmTypes.takeLast(1)))
    dpmService.updateDpms(updateGroups)
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(DpmServiceTest::class.java)
  }
}
