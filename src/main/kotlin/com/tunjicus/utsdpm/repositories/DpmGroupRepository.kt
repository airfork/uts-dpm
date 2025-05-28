package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.DpmGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DpmGroupRepository : JpaRepository<DpmGroup, Int> {
  fun findAllByActiveTrue(): List<DpmGroup>

  @Modifying
  @Query("UPDATE DpmGroup d SET d.active = false WHERE d.id in ?1")
  fun deactivateAllIn(ids: List<Int>)
}
