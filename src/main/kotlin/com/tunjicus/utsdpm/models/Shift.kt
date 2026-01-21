package com.tunjicus.utsdpm.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Shift(
  @field:JsonProperty("PUBLISHED")
  val published: String,

  @field:JsonProperty("FIRST_NAME")
  val firstName: String,

  @field:JsonProperty("LAST_NAME")
  val lastName: String,

  @field:JsonProperty("START_DATE")
  val startDate: String,

  @field:JsonProperty("END_DATE")
  val endDate: String,

  @field:JsonProperty("START_TIME")
  val startTime: String,

  @field:JsonProperty("END_TIME")
  val endTime: String,

  @field:JsonProperty("DESCRIPTION")
  val description: String,

  @field:JsonProperty("COLOR_ID")
  val colorId: String,

  @field:JsonProperty("POSITION_NAME")
  val block: String
)
