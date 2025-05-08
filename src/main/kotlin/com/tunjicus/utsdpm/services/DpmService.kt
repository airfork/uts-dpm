package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.DpmGroup
import com.tunjicus.utsdpm.repositories.DpmGroupRepository
import com.tunjicus.utsdpm.repositories.DpmRepository
import org.springframework.stereotype.Service

@Service
class DpmService(
    private val dpmGroupRepository: DpmGroupRepository,
    private val dpmRepository: DpmRepository
) {
  fun getDpmGroups() = dpmGroupRepository.findAllByOrderByGroupName()

//  fun getGroupWithDpms(id: Int) = dpmGroupRepository.findWithDpmsById(id)

  fun createGroup(name: String) = dpmGroupRepository.save(DpmGroup().apply { this.groupName = name })

  fun createDpm(name: String, points: Int, group: DpmGroup) = dpmRepository.save(
    Dpm().apply {
      this.dpmName = name
      this.points = points
      this.dpmGroup = group
    }
  )
}
