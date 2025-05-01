package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.entities.Dpm
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
    val type: String,
    val points: Int,
    val notes: String
) {
  fun toDpm(): Dpm {
    val dpm = Dpm()
    dpm.block = block
    dpm.date = TimeService.getTodayDate()
    dpm.dpmType = type
    dpm.location = location
    dpm.notes = notes
    dpm.points = points
    dpm.startTime = FormatHelpers.inboundDpmTime(startTime)
    dpm.endTime = FormatHelpers.inboundDpmTime(endTime)
    return dpm
  }

  companion object {
    private const val GOOD_DPM_VALUE = 1
    private const val BAD_DPM_VALUE = -10

    fun from(shift: Shift): AutogenDpm? {
      val color = ShiftColor.from(shift.colorId)
      if (color == ShiftColor.UNTRACKED) return null

      val points: Int
      val type: String
      if (color == ShiftColor.GOLD) {
        type = "Picked Up Block"
        points = GOOD_DPM_VALUE
      } else {
        type = "DNS/Did Not Show"
        points = BAD_DPM_VALUE
      }

      val name = "${shift.firstName} ${shift.lastName}".trim()
      val block = shift.block
      val startTime = FormatHelpers.convertW2WTime(shift.startTime)
      val endTime = FormatHelpers.convertW2WTime(shift.endTime)
      val (location, notes) = parseDescription(shift.description)

      return AutogenDpm(name, block, startTime, endTime, location, type, points, notes)
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
