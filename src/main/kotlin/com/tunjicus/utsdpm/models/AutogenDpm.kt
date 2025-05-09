package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.UserDpm
import com.tunjicus.utsdpm.enums.ShiftColor
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.services.TimeService

class AutogenDpm
private constructor(
    val name: String,
    val block: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val type: Dpm,
    val notes: String
) {
  fun toDpm(): UserDpm {
    val userDpm = UserDpm()
    userDpm.block = block
    userDpm.date = TimeService.getTodayDate()
    userDpm.dpmType = type
    userDpm.location = location
    userDpm.notes = notes
    userDpm.points = type.points
    userDpm.startTime = FormatHelpers.inboundDpmTime(startTime)
    userDpm.endTime = FormatHelpers.inboundDpmTime(endTime)
    return userDpm
  }

  companion object {
    fun hasAllColors(w2wDpms: List<Dpm>): Boolean {
      val requiredColors = listOf(ShiftColor.GOLD, ShiftColor.RED)
      return requiredColors.all { color -> w2wDpms.any { it.w2wColorCode == color.code } }
    }

    fun from(shift: Shift, w2wDpms: List<Dpm>): AutogenDpm? {
      val color = ShiftColor.from(shift.colorId)
      if (color == ShiftColor.UNTRACKED || !hasAllColors(w2wDpms)) return null

      val dpmType = w2wDpms.find { it.w2wColorCode == color.code }!!
      val name = "${shift.firstName} ${shift.lastName}".trim()
      val block = shift.block
      val startTime = FormatHelpers.convertW2WTime(shift.startTime)
      val endTime = FormatHelpers.convertW2WTime(shift.endTime)
      val (location, notes) = parseDescription(shift.description)

      return AutogenDpm(name, block, startTime, endTime, location, dpmType, notes)
    }

    fun parseDescription(description: String): Pair<String, String> {
      val location: String =
          if (description.startsWith("[")) {
            "OTR"
          } else {
            description.split(" ")[0]
          }

      return Pair(location, extractFromBraces(description))
    }

    fun extractFromBraces(input: String): String {
      // Case 1: Complete braces {}
      val completeBracePattern = "\\{([^}]*)}".toRegex()
      val matchResult = completeBracePattern.find(input)

      if (matchResult != null) {
        // Return text between the braces
        return matchResult.groupValues[1]
      }

      // Case 2: Only opening brace {
      val openingBraceIndex = input.indexOf('{')
      if (openingBraceIndex >= 0 && openingBraceIndex < input.length - 1) {
        // Return everything after the opening brace
        return input.substring(openingBraceIndex + 1)
      }

      // No braces found
      return ""
    }
  }
}
