package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.helpers.FormatHelpers

data class DpmReceivedEmail(
  val name: String,
  val dpmType: String,
  val receivedDate: String,
  val manager: String,
  val url: String
) {
  private val year: String = FormatHelpers.currentYear()

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
