package com.tunjicus.utsdpm.enums

enum class ShiftColor(val code: String) {
  GOLD("14"),
  RED("9"),
  UNTRACKED("-1");

  companion object {
    fun from(findValue: String): ShiftColor = entries.find { it.code == findValue } ?: UNTRACKED
  }
}
