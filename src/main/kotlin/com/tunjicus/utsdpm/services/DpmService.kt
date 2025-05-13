package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.GetDpmGroupDto
import com.tunjicus.utsdpm.dtos.GetDpmTypeDto
import com.tunjicus.utsdpm.dtos.PutDpmGroupDto
import com.tunjicus.utsdpm.dtos.PutDpmTypeDto
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.DpmOrder
import com.tunjicus.utsdpm.models.DpmGroupOrder
import com.tunjicus.utsdpm.repositories.DpmGroupRepository
import com.tunjicus.utsdpm.repositories.DpmOrderRepository
import com.tunjicus.utsdpm.repositories.DpmRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class DpmService(
    private val dpmGroupRepository: DpmGroupRepository,
    private val dpmRepository: DpmRepository,
    private val dpmOrderRepository: DpmOrderRepository,
    private val objectMapper: ObjectMapper
) {
  fun getDpmGroupList(): List<GetDpmGroupDto> {
    val dpmGroups = dpmGroupRepository.findAllByActiveTrue()
    val order = dpmOrderRepository.findTopByOrderByUpdatedAtDesc()
    if (order == null) return dpmGroups.map { createUnorderedDpmGroupDto(it) }

    val typeReference = object : TypeReference<List<DpmGroupOrder>>() {}
    val groupOrder = objectMapper.readValue(order.dpmOrder!!, typeReference)
    val outputOrder = mutableListOf<GetDpmGroupDto>()

    for (groupInfo in groupOrder) {
      val group = dpmGroups.find { it.id == groupInfo.group }
      if (group == null || group.dpms == null || group.dpms!!.isEmpty()) continue
      outputOrder.add(createDpmGroupDto(group, groupOrder.find { it.group == group.id }))
    }

    return outputOrder
  }

  @Transactional
  fun updateDpms(newGroups: List<PutDpmGroupDto>) {
    val existingGroups = dpmGroupRepository.findAllByActiveTrue()
    val existingDpms = dpmRepository.findAllByActiveTrue()

    val activeGroups = mutableListOf<DpmGroup>()
    val activeDpms = mutableListOf<Dpm>()

    for (groupInfo in newGroups) {
      val group = findOrCreateNewGroup(groupInfo.groupName, existingGroups)
      activeGroups.add(group)

      assert(group.dpms != null)

      group.dpms?.clear()
      for (dpmInfo in groupInfo.dpms) {
        val foundDpm = findOrCreateNewDpm(dpmInfo, existingDpms)
        group.dpms?.add(foundDpm)
        activeDpms.add(foundDpm)
        foundDpm.dpmGroup = group
      }
    }

    val inactiveDpms = existingDpms.filter { !activeDpms.contains(it) }
    inactiveDpms.forEach { it.active = false }

    val inactiveGroups = existingGroups.filter { !activeGroups.contains(it) }
    inactiveGroups.forEach { it.active = false }

    dpmRepository.deactivateAllIn(inactiveDpms.map { it.id!! })
    dpmGroupRepository.deactivateAllIn(inactiveGroups.map { it.id!! })

    createDpmGroupOrder(dpmGroupRepository.saveAll(activeGroups))
  }

  private fun createDpmGroupOrder(groups: List<DpmGroup>) {
    val dpmOrder = dpmOrderRepository.findTopByOrderByUpdatedAtDesc() ?: DpmOrder()
    val orders = mutableListOf<DpmGroupOrder>()
    for (group in groups) {
      orders.add(DpmGroupOrder(group.id!!, group.dpms?.map { it.id!! } ?: emptyList()))
    }

    dpmOrder.dpmOrder = objectMapper.writeValueAsString(orders)
    dpmOrderRepository.save(dpmOrder)
  }

  private fun findOrCreateNewDpm(dpm: PutDpmTypeDto, existingDpms: List<Dpm>): Dpm {
    val existingDpm = existingDpms.find { it.dpmName == dpm.dpmType && it.points == dpm.points }
    if (existingDpm != null) return existingDpm
    return Dpm().apply {
      this.dpmName = dpm.dpmType
      this.points = dpm.points
    }
  }

  private fun findOrCreateNewGroup(groupName: String, existingGroups: List<DpmGroup>): DpmGroup {
    val existingGroup = existingGroups.find { it.groupName == groupName }
    if (existingGroup != null) return existingGroup
    return dpmGroupRepository.save(DpmGroup().apply { this.groupName = groupName })
  }

  private fun createDpmGroupDto(group: DpmGroup, order: DpmGroupOrder?): GetDpmGroupDto {
    if (order == null || group.dpms == null) return createUnorderedDpmGroupDto(group)
    val orderList = mutableListOf<GetDpmTypeDto>()
    val activeDpms = group.dpms!!.filter { it.active }

    for (id in order.dpms) {
      val dpm = activeDpms.find { it.id == id } ?: continue
      orderList.add(GetDpmTypeDto.from(dpm))
    }

    return GetDpmGroupDto(group.groupName, orderList)
  }

  private fun createUnorderedDpmGroupDto(group: DpmGroup) =
      GetDpmGroupDto(group.groupName, group.dpms?.map { GetDpmTypeDto.from(it) } ?: emptyList())
}
