package com.tunjicus.utsdpm.emailModels

import com.tunjicus.utsdpm.helpers.formatCurrentYear

data class PointsBalance(val name: String, val manager: String, val points: Int) {
  private val year: String = formatCurrentYear()
  fun toMap(): Map<String, String> =
    mapOf(
      Pair("name", name),
      Pair("manager", manager),
      Pair("points", points.toString()),
      Pair("year", year)
    )
}