package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Dpm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DpmRepository : JpaRepository<Dpm, Int> {
  @Query(
      value = "select * from dpms where active=true and w2w_color_code is not null",
      nativeQuery = true)
  fun findW2WDpms(): List<Dpm>
}
