package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.helpers.FormatHelpers

data class PointsBalanceEmail(val name: String, val manager: String, val points: Int) {
  private val year: String = FormatHelpers.currentYear()
  fun toMap(): Map<String, String> =
    mapOf(
      Pair("name", name),
      Pair("manager", manager),
      Pair("points", points.toString()),
      Pair("year", year)
    )
}
