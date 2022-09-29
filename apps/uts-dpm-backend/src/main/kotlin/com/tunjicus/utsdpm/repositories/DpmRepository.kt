package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface DpmRepository : PagingAndSortingRepository<Dpm, Int> {
  @Query(
    value = "select * from dpms " +
      "where userid = :id and created >= :created " +
      "and approved = true and ignored is distinct from true " +
      "order by created desc",
    nativeQuery = true
  )
  fun getCurrentDpms(
    @Param("id") userId: Int,
    @Param("created") created: LocalDateTime
  ): Collection<Dpm>

  @Query(
    value = "select * from dpms " +
      "where approved is distinct from true " +
      "and ignored is distinct from true " +
      "order by created desc",
    nativeQuery = true
  )
  fun getUnapprovedDpms(): Collection<Dpm>

  fun findAllByCreatedAfterAndCreatedBeforeOrderByCreatedDesc(
    after: LocalDateTime,
    before: LocalDateTime
  ): Collection<Dpm>

  fun findAllByUserOrderByCreatedDesc(user: User, pageable: Pageable): Page<Dpm>
}
