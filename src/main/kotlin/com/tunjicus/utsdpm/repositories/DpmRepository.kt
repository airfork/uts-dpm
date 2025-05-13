package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Dpm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DpmRepository : JpaRepository<Dpm, Int> {
  fun findAllByActiveTrueAndW2wColorCodeNotNull(): List<Dpm>

  fun findAllByActiveTrue(): List<Dpm>

  @Modifying
  @Query("UPDATE Dpm d SET d.active = false WHERE d.id in ?1")
  fun deactivateAllIn(ids: List<Int>)
}
