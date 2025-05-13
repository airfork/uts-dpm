package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.Dpm

data class GetDpmTypeDto(val id: Int, val name: String, val points: Int) {
  companion object {
    fun from(dpmType: Dpm) = GetDpmTypeDto(dpmType.id!!, dpmType.dpmName, dpmType.points!!)
  }
}
