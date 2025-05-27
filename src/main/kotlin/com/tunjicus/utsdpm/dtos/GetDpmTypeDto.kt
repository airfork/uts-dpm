package com.tunjicus.utsdpm.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.tunjicus.utsdpm.entities.Dpm

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetDpmTypeDto(
    val id: Int,
    val name: String,
    val points: Int,
    val dpmColor: DpmColor? = null
) {
  companion object {
    fun from(dpmType: Dpm) =
        GetDpmTypeDto(
            dpmType.id!!,
            dpmType.dpmName,
            dpmType.points!!,
            dpmType.w2wColor?.let { DpmColor(it.id!!, it.hexCode) })
  }

  data class DpmColor(val colorId: Int, val hexCode: String)
}
