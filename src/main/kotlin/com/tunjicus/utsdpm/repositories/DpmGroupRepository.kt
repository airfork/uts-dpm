package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.DpmGroup
import org.springframework.data.jpa.repository.JpaRepository

interface DpmGroupRepository : JpaRepository<DpmGroup, Int> {
  fun findAllByOrderByGroupName(): List<DpmGroup>
}
