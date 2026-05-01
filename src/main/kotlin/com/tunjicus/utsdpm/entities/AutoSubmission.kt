package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import java.time.LocalDate
import java.time.ZonedDateTime
import jakarta.persistence.*

@Entity
@Table(
    name = "auto_submissions",
    uniqueConstraints =
        [UniqueConstraint(name = "auto_submissions_submitted_date_key", columnNames = ["submitted_date"])])
class AutoSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "auto_submission_id")
  var id: Int? = null

  @Column(name = "submitted", updatable = false)
  var submitted: ZonedDateTime = TimeService.getTodayZonedDateTime()

  @Column(name = "submitted_date", nullable = false, updatable = false)
  var submittedDate: LocalDate = TimeService.getTodayDate()

  companion object {
    fun min() =
        AutoSubmission().apply {
          submitted = ZonedDateTime.now().minusYears(2000)
          submittedDate = submitted.withZoneSameInstant(TimeService.ZONE_ID).toLocalDate()
        }
  }
}
