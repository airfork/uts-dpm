package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.User
import java.time.ZonedDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface DpmRepository : PagingAndSortingRepository<Dpm, Int>, CrudRepository<Dpm, Int> {
  @Query(
    value =
      "select * from dpms " +
        "where userid = :id and created >= :created " +
        "and approved = true and ignored is distinct from true " +
        "order by created desc",
    nativeQuery = true
  )
  fun getCurrentDpms(
    @Param("id") userId: Int,
    @Param("created") created: ZonedDateTime
  ): Collection<Dpm>

  @Query(
    value =
      "select * from dpms " +
        "where approved is distinct from true " +
        "and ignored is distinct from true " +
        "order by created desc",
    nativeQuery = true
  )
  fun getUnapprovedDpms(pageable: Pageable): Page<Dpm>

  @Query(
    value =
    """
      select d.id,
             d.createid,
             d.userid,
             d.block,
             d.date,
             d.dpmtype,
             d.points,
             d.notes,
             d.created,
             d.approved,
             d.location,
             d.starttime,
             d.endtime,
             d.ignored
      from dpms d
               inner join users u on u.id = d.userid
      where d.approved is distinct from true
        and d.ignored is distinct from true
        and u.managerid = :managerId
      order by d.created desc
    """,
    nativeQuery = true
  )
  fun getUnapprovedDpms(managerId: Int, pageable: Pageable): Page<Dpm>

  fun findAllByCreatedAfterAndCreatedBeforeOrderByCreatedDesc(
    after: ZonedDateTime,
    before: ZonedDateTime
  ): Collection<Dpm>

  fun findAllByUserOrderByCreatedDesc(user: User, pageable: Pageable): Page<Dpm>

  @Modifying
  @Query(
    "UPDATE dpms d " +
      "SET ignored = TRUE " +
      "WHERE d.userid IN " +
      "(SELECT id FROM users WHERE fulltime = FALSE) " +
      "AND d.approved = TRUE",
    nativeQuery = true
  )
  fun ignorePartTimerDpms()

  @Modifying fun deleteByUser(user: User)

  @Modifying
  @Query("UPDATE Dpm d SET d.createdUser=:new WHERE d.createdUser=:old")
  fun changeCreatedUser(new: User, old: User)

  @Modifying
  @Query("UPDATE Dpm d set d.ignored=true WHERE d.approved=false AND d.user=:user")
  fun ignoreUnapproved(user: User)
}
