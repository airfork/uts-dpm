package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.models.AutogenDpm

data class AutogenDpmDto(
  val name: String,
  val block: String,
  val startTime: String,
  val endTime: String,
  val type: String,
) {
  companion object {
    fun from(dpm: AutogenDpm) =
      AutogenDpmDto(dpm.name, dpm.block, dpm.startTime, dpm.endTime, dpm.type)
  }
}
