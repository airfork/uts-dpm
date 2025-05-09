package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm

data class DpmTypeDto(val id: Int, val name: String, val points: Int) {
  companion object {
    fun from(dpmType: Dpm) = DpmTypeDto(dpmType.id!!, dpmType.dpmName, dpmType.points!!)
  }
}
