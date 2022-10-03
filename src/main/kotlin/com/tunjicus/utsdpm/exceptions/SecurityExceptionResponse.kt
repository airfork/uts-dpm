package com.tunjicus.utsdpm.exceptions

import java.time.ZonedDateTime

class SecurityExceptionResponse private constructor(
  val timestamp: ZonedDateTime,
  val status: Int,
  val error: String,
  val path: String
)
