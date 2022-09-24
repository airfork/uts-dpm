package com.tunjicus.utsdpm.dtos

data class AutogenDpm(
  val name: String,
  val block: String,
  val startTime: String,
  val endTime: String,
  val location: String,
  val type: String,
  val points: Int,
  val notes: String
)
