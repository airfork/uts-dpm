package com.tunjicus.utsdpm.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tunjicus.utsdpm.dtos.DpmGroupDto
import com.tunjicus.utsdpm.dtos.DpmTypeDto
import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.models.DpmGroupOrder
import com.tunjicus.utsdpm.repositories.DpmGroupRepository
import com.tunjicus.utsdpm.repositories.DpmOrderRepository
import com.tunjicus.utsdpm.repositories.DpmRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DpmService(
    private val dpmGroupRepository: DpmGroupRepository,
    private val dpmRepository: DpmRepository,
    private val dpmOrderRepository: DpmOrderRepository,
    private val objectMapper: ObjectMapper
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(DpmService::class.java)
  }

  fun getDpmGroups() = dpmGroupRepository.findAllByOrderByGroupName()

  fun createGroup(name: String) =
      dpmGroupRepository.save(DpmGroup().apply { this.groupName = name })

  fun createDpm(name: String, points: Int, group: DpmGroup) =
      dpmRepository.save(
          Dpm().apply {
            this.dpmName = name
            this.points = points
            this.dpmGroup = group
          })

  fun getDpmGroupList(): List<DpmGroupDto> {
    val dpmGroups = dpmGroupRepository.findAll()
    val order = dpmOrderRepository.findTopByOrderByUpdatedAtDesc()
    if (order == null) return dpmGroups.map { createUnorderedDpmGroupDto(it) }

    val typeReference = object : TypeReference<List<DpmGroupOrder>>() {}
    val groupOrder = objectMapper.readValue(order.dpmOrder!!, typeReference)
    val outputOrder = mutableListOf<DpmGroupDto>()

    LOGGER.info("Iterating over groups")
    for (groupInfo in groupOrder) {
      val group = dpmGroups.find { it.id == groupInfo.group }
      if (group == null || group.dpms == null || group.dpms!!.isEmpty()) continue
      outputOrder.add(createDpmGroupDto(group, groupOrder.find { it.group == group.id }))
    }

    return outputOrder
  }

  private fun createDpmGroupDto(group: DpmGroup, order: DpmGroupOrder?): DpmGroupDto {
    if (order == null || group.dpms == null) return createUnorderedDpmGroupDto(group)
    val orderList = mutableListOf<DpmTypeDto>()
    val activeDpms = group.dpms!!.filter { it.active }

    for (id in order.dpms) {
      val dpm = activeDpms.find { it.id == id } ?: continue
      orderList.add(DpmTypeDto.from(dpm))
    }

    return DpmGroupDto(group.groupName, orderList)
  }

  private fun createUnorderedDpmGroupDto(group: DpmGroup) =
      DpmGroupDto(group.groupName, group.dpms?.map { DpmTypeDto.from(it) } ?: emptyList())
}
