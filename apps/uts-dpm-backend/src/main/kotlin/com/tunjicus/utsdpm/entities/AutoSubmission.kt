package com.tunjicus.utsdpm.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "auto_submissions")
class AutoSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "auto_submission_id")
  var id: Int? = null

  @Column(name = "submitted", insertable = false, updatable = false)
  var submitted: LocalDateTime? = null

  companion object {
    fun min() = AutoSubmission().apply { submitted = LocalDateTime.MIN }
  }
}
