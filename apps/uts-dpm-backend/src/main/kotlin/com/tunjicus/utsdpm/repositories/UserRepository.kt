package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserRepository : CrudRepository<User, Int> {
  @Query(
    value = "select concat(firstname, ' ', lastname) as name from users order by lastname, firstname",
    nativeQuery = true
  )
  fun getAllNames(): Collection<String>

  @Query(value = "select * from users where concat(firstname, ' ', lastname) = :name LIMIT 1", nativeQuery = true)
  fun findByFullName(@Param("name") name: String): Optional<User>
}
