package com.tunjicus.utsdpm.models

import com.fasterxml.jackson.annotation.JsonProperty

data class Shift(
  @JsonProperty("PUBLISHED")
   val published: String,

  @JsonProperty("FIRST_NAME")
  val firstName: String,

  @JsonProperty("LAST_NAME")
  val lastName: String,

  @JsonProperty("START_DATE")
  val startDate: String,

  @JsonProperty("END_DATE")
  val endDate: String,

  @JsonProperty("START_TIME")
  val startTime: String,

  @JsonProperty("END_TIME")
  val endTime: String,

  @JsonProperty("DESCRIPTION")
  val description: String,

  @JsonProperty("COLOR_ID")
  val colorId: String,

  @JsonProperty("POSITION_NAME")
  val block: String
)
