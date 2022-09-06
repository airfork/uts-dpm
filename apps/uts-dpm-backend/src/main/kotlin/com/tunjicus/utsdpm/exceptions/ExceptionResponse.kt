package com.tunjicus.utsdpm.exceptions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.OffsetDateTime
import java.time.ZoneId

data class ExceptionResponse(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneId.of("UTC")),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
