package com.tunjicus.utsdpm.exceptions

import com.tunjicus.utsdpm.services.TimeService
import java.time.ZonedDateTime

data class ExceptionResponse(
    val timestamp: ZonedDateTime = TimeService.getTodayZonedDateTime(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
