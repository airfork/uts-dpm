package com.tunjicus.utsdpm.models

import com.fasterxml.jackson.annotation.JsonProperty

data class AssignedShifts(
  @JsonProperty("AssignedShiftList")
  val shifts: List<Shift>
)
