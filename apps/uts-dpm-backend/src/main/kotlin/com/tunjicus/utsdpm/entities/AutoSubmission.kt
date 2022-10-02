package com.tunjicus.utsdpm.entities

import com.tunjicus.utsdpm.services.TimeService
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "auto_submissions")
class AutoSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "auto_submission_id")
  var id: Int? = null

  @Column(name = "submitted", updatable = false)
  var submitted: ZonedDateTime = TimeService.getTodayZonedDateTime()

  companion object {
    fun min() = AutoSubmission().apply { submitted = ZonedDateTime.now().minusYears(2000) }
  }
}
