package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.entities.UserDpm
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

interface UserDpmRepository : PagingAndSortingRepository<UserDpm, Int>, CrudRepository<UserDpm, Int> {
  @Query(
    value =
      "select * from user_dpms " +
        "where user_id = :id and created >= :created " +
        "and approved = true and ignored is distinct from true " +
        "order by created desc",
    nativeQuery = true
  )
  fun getCurrentDpms(
    @Param("id") userId: Int,
    @Param("created") created: ZonedDateTime
  ): Collection<UserDpm>

  @Query(
    value =
      "select * from user_dpms " +
        "where approved is distinct from true " +
        "and ignored is distinct from true " +
        "order by created desc",
    nativeQuery = true
  )
  fun getUnapprovedDpms(pageable: Pageable): Page<UserDpm>

  @Query(
    value =
    """
      select ud.user_dpm_id,
       ud.create_id,
       ud.user_id,
       ud.block,
       ud.date,
       d.name,
       ud.points,
       ud.notes,
       ud.created,
       ud.approved,
       ud.location,
       ud.start_time,
       ud.end_time,
       ud.ignored
      from user_dpms ud
               inner join dpms d on d.dpm_id = ud.dpm_id
               inner join users u on u.id = ud.user_id
      where ud.approved is distinct from true
        and ud.ignored is distinct from true
        and u.managerid = :managerId
      order by ud.created desc
    """,
    nativeQuery = true
  )
  fun getUnapprovedDpms(managerId: Int, pageable: Pageable): Page<UserDpm>

  fun findAllByCreatedAfterAndCreatedBeforeOrderByCreatedDesc(
    after: ZonedDateTime,
    before: ZonedDateTime
  ): Collection<UserDpm>

  fun findAllByUserOrderByCreatedDesc(user: User, pageable: Pageable): Page<UserDpm>

  @Modifying
  @Query(
    "UPDATE user_dpms d " +
      "SET ignored = TRUE " +
      "WHERE d.user_id IN " +
      "(SELECT id FROM users WHERE fulltime = FALSE) " +
      "AND d.approved = TRUE",
    nativeQuery = true
  )
  fun ignorePartTimerDpms()

  @Modifying fun deleteByUser(user: User)

  @Modifying
  @Query("UPDATE UserDpm d SET d.createdUser=:new WHERE d.createdUser=:old")
  fun changeCreatedUser(new: User, old: User)

  @Modifying
  @Query("UPDATE UserDpm d set d.ignored=true WHERE d.approved=false AND d.user=:user")
  fun ignoreUnapproved(user: User)
}
