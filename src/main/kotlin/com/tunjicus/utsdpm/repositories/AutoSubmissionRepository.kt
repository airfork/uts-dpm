package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.AutoSubmission
import java.time.LocalDate
import java.time.ZonedDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface AutoSubmissionRepository : JpaRepository<AutoSubmission, Int> {

  @Query(
    "select * from auto_submissions a " +
      "where a.submitted = " +
      "(select max(s.submitted) from auto_submissions s)",
    nativeQuery = true
  )
  fun findMostRecent(): AutoSubmission?

  fun existsBySubmittedDate(submittedDate: LocalDate): Boolean

  @Modifying fun deleteBySubmittedBefore(submitted: ZonedDateTime): Int
}
