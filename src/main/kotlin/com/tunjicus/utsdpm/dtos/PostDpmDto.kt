package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.helpers.formatInboundDpmDate
import com.tunjicus.utsdpm.helpers.formatInboundDpmTime
import com.tunjicus.utsdpm.services.DpmService
import com.tunjicus.utsdpm.validators.ValidDateFormat
import com.tunjicus.utsdpm.validators.ValidType
import org.hibernate.validator.constraints.Length
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class PostDpmDto(
  @field:NotBlank(message = "driver cannot be blank")
  val driver: String?,

  @field:NotBlank(message = "block cannot be blank")
  val block: String?,

  @field:NotBlank(message = "date cannot be blank")
  @field:ValidDateFormat(value = "MM/dd/yyyy", message = "Unable to parse date, must be in format MM/dd/yyyy")
  val date: String?,

  @field:NotBlank(message = "type cannot be blank")
  @field:ValidType(message = "type is not a valid DPM type")
  val type: String?,

  @field:NotBlank(message = "location cannot be blank")
  @field:Length(max = 10, message = "location cannot be longer than 10 characters")
  val location: String?,

  @field:NotBlank(message = "startTime cannot be blank")
  @field:Pattern(
    regexp = "^(?:[01][0-9]|2[0-3])[0-5][0-9](?::[0-5][0-9])?\$",
    message = "startTime is not valid, expected HHmm"
  )
  val startTime: String?,

  @field:NotBlank(message = "endTime cannot be blank")
  @field:Pattern(
    regexp = "^(?:[01][0-9]|2[0-3])[0-5][0-9](?::[0-5][0-9])?\$",
    message = "endTime is not valid, expected HHmm"
  )
  val endTime: String?,
  val notes: String?
) {
  fun toDpm(): Dpm {
    val dpm = Dpm()
    dpm.block = block
    dpm.date = formatInboundDpmDate(date)
    dpm.dpmType = DpmService.stripPointsFromType(type!!)
    dpm.location = location
    dpm.notes = notes
    dpm.points = DpmService.pointsForType(type)
    dpm.startTime = formatInboundDpmTime(startTime)
    dpm.endTime = formatInboundDpmTime(endTime)
    return dpm
  }
}
