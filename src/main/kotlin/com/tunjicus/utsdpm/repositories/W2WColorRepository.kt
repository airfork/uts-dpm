package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.W2WColor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface W2WColorRepository : JpaRepository<W2WColor, Int> {
  @Query("from W2WColor c join c.dpms d where c.active and d.active")
  fun findAllActiveWithDpms(): List<W2WColor>

  fun findAllByActiveTrue(): List<W2WColor>
}
