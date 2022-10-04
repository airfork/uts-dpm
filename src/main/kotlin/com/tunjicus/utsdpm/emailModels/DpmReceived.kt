package com.tunjicus.utsdpm.emailModels

import com.tunjicus.utsdpm.helpers.formatCurrentYear

data class DpmReceived(
  val name: String,
  val dpmType: String,
  val receivedDate: String,
  val manager: String,
  val url: String
) {
  private val year: String = formatCurrentYear()

  fun toMap(): Map<String, String> =
    mapOf(
      Pair("name", name),
      Pair("dpmType", dpmType),
      Pair("receivedDate", receivedDate),
      Pair("manager", manager),
      Pair("url", url),
      Pair("year", year)
    )
}
