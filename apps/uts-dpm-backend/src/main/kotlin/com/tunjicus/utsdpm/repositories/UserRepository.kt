package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.User
import java.util.Optional
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : CrudRepository<User, Int> {
  @Query(
    value = "FROM User WHERE CONCAT(firstname, ' ', lastname) = :name",
  )
  fun findByFullName(@Param("name") name: String): User?

  @Query("FROM User ORDER BY lastname, firstname") fun findAllSorted(): Collection<User>

  @Query(
    "SELECT CONCAT(u.firstname, ' ', u.lastname) FROM users u " +
      "INNER JOIN user_roles ur ON u.id = ur.user_id " +
      "INNER JOIN roles r ON r.role_id = ur.role_id " +
      "WHERE r.name = 'MANAGER' " +
      "OR r.name = 'ADMIN' " +
      "ORDER BY lastname, firstname",
    nativeQuery = true
  )
  fun findAllManagers(): Collection<String>
}
