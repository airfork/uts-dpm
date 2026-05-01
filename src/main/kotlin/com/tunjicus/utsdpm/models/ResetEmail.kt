package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.helpers.FormatHelpers

class ResetEmail(
  private val name: String,
  private val resetUrl: String,
) {
  private val year: String = FormatHelpers.currentYear()

  fun toMap(): Map<String, String> =
    mapOf(Pair("name", name), Pair("resetUrl", resetUrl), Pair("year", year))
}
