package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.DpmOrder
import org.springframework.data.jpa.repository.JpaRepository

interface DpmOrderRepository : JpaRepository<DpmOrder, Int> {
  fun findTopByOrderByUpdatedAtDesc(): DpmOrder?
}
