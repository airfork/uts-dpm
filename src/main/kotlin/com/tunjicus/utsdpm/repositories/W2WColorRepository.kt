package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.W2WColor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface W2WColorRepository : JpaRepository<W2WColor, Int> {
  @Query("SELECT DISTINCT c FROM W2WColor c JOIN c.dpms d WHERE c.active = true AND d.active = true")
  fun findAllActiveWithDpms(): List<W2WColor>

  fun findAllByActiveTrue(): List<W2WColor>
}
