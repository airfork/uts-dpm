package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.AutoSubmission
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface AutoSubmissionRepository : CrudRepository<AutoSubmission, Int> {

  @Query(
    "select * from auto_submissions a " +
      "where a.submitted = " +
      "(select max(s.submitted) from auto_submissions s)",
    nativeQuery = true
  )
  fun findMostRecent(): AutoSubmission?

  @Modifying
  fun deleteBySubmittedBefore(timestamp: LocalDateTime): Int
}
