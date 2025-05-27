package com.tunjicus.utsdpm.dtos

import com.tunjicus.utsdpm.entities.W2WColor

data class GetW2WColors(val colorId: Int, val colorName: String, val hexCode: String) {
  companion object {
    fun from(color: W2WColor) = GetW2WColors(color.id!!, color.colorName, color.hexCode)
  }
}
