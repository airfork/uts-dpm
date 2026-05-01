package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.enums.RoleName
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : CrudRepository<User, Int> {
  @Query(
    value = "FROM User WHERE CONCAT(firstname, ' ', lastname) = :name",
  )
  fun findByFullName(@Param("name") name: String): User?

  @Query("FROM User ORDER BY lastname, firstname") fun findAllSorted(): Collection<User>

  @Query("FROM User u WHERE u.role.roleName IN :roleNames ORDER BY u.lastname, u.firstname")
  fun findAllManagers(@Param("roleNames") roleNames: Collection<RoleName>): Collection<User>

  fun findByUsername(username: String): User?

  fun existsByUsername(username: String): Boolean

  @Modifying
  @Query("UPDATE User u SET u.points=0 WHERE u.fullTime=false")
  fun resetPartTimerPoints()

  @Modifying
  @Query("UPDATE User u SET u.manager=:new WHERE u.manager=:old")
  fun changeManager(new: User, old: User)
}
