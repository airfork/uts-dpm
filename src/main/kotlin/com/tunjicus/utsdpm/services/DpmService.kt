package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.*
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.entities.DpmOrder
import com.tunjicus.utsdpm.exceptions.InvalidDpmGroupUpdateException
import com.tunjicus.utsdpm.models.DpmGroupOrder
import com.tunjicus.utsdpm.repositories.DpmGroupRepository
import com.tunjicus.utsdpm.repositories.DpmOrderRepository
import com.tunjicus.utsdpm.repositories.DpmRepository
import com.tunjicus.utsdpm.repositories.W2WColorRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class DpmService(
    private val dpmGroupRepository: DpmGroupRepository,
    private val dpmRepository: DpmRepository,
    private val dpmOrderRepository: DpmOrderRepository,
    private val w2wColorRepository: W2WColorRepository,
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
    if (newGroups.isEmpty()) return
    if (newGroups.size > 50)
        throw InvalidDpmGroupUpdateException("Cannot update more than 50 groups.")

    val existingGroups = dpmGroupRepository.findAllByActiveTrue()
    val existingDpms = dpmRepository.findAllByActiveTrue()

    val activeGroups = mutableListOf<DpmGroup>()
    val activeDpms = mutableListOf<Dpm>()

    val colorsInUpdate = mutableSetOf<Int>()
    val groupNames = mutableSetOf<String>()

    for (groupInfo in newGroups) {
      if (!groupNames.add(groupInfo.groupName.lowercase().trim())) {
        throw InvalidDpmGroupUpdateException(
            "Group names must be unique; '${groupInfo.groupName}' was given twice.")
      }

      val group = findOrCreateNewGroup(groupInfo.groupName, existingGroups)
      activeGroups.add(group)
      assert(group.dpms != null)

      val dpmNames = mutableSetOf<String>()
      group.dpms?.clear()
      for (dpmInfo in groupInfo.dpms) {
        if (!dpmNames.add(dpmInfo.dpmType.lowercase().trim())) {
          throw InvalidDpmGroupUpdateException(
              "DPM names must be unique inside of their group; '${dpmInfo.dpmType}' was given twice.")
        }

        val foundDpm = findOrCreateNewDpm(dpmInfo, existingDpms)
        if (dpmInfo.colorId != null) {
          val color =
              w2wColorRepository.findById(dpmInfo.colorId).orElseThrow {
                InvalidDpmGroupUpdateException("Color id ${dpmInfo.colorId} does not exist.")
              }

          foundDpm.w2wColor = color
          if (!colorsInUpdate.add(color.id!!)) {
            throw InvalidDpmGroupUpdateException(
                "A color can only be assigned to 1 DPM, but ${color.id} was assigned to multiple.")
          }
        } else {
          foundDpm.w2wColor = null
        }

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

  fun getColors(): List<GetW2WColors> =
      w2wColorRepository
          .findAllByActiveTrue()
          .sortedBy { it.colorName }
          .map { GetW2WColors.from(it) }

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
