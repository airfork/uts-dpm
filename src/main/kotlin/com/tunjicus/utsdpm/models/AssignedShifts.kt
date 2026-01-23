package com.tunjicus.utsdpm.models

import com.fasterxml.jackson.annotation.JsonProperty

data class AssignedShifts(
  @field:JsonProperty("AssignedShiftList")
  val shifts: List<Shift>
)
