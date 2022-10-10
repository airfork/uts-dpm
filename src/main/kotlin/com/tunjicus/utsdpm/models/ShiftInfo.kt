package com.tunjicus.utsdpm.models

// sample shift
// swl("952294753",2,"#000000","Brian Newman","959635624","07:00 - 14:20", "7.33 hours","OFF");
class ShiftInfo(val block: String, shiftSplit: List<String>) {
  private val shiftColor: String
  private val timeRange: String
  private val locationAndNotes: String

  val name: String
  val startTime: String
  val endTime: String
  val location: String

  init {
    shiftColor = shiftSplit[2].trim('"').trimStart('#').lowercase()
    name = shiftSplit[3].trim('"')
    timeRange = shiftSplit[5].trim('"')
    startTime = timeRange.substring(0, 5).replace(":", "")
    endTime = timeRange.substring(8).replace(":", "")
    locationAndNotes = shiftSplit[7].trim('"').trim()
    location = setLocation()
  }

  fun isValid(): Pair<Boolean, String> {
    if (!isShiftColorValid()) {
      return Pair(false, "")
    }

    if (!isTimeRangeValid()) {
      return Pair(false, "Error getting time")
    }

    if (!isLocationAndNotesValid()) {
      return Pair(false, "Error getting location")
    }

    return Pair(true, "")
  }

  fun getDPMType(): DPMType {
    if (isGood()) return DPMType.good()

    val dnsIndex = locationAndNotes.indexOf("DNS", ignoreCase = true)
    val notes =
      if (dnsIndex == -1) ""
      else {
        locationAndNotes
          .substring(dnsIndex + 3)
          .replace("(", "")
          .replace(")", "")
          .replace("\"", "")
          .trim()
      }

    return DPMType.bad(notes)
  }

  private fun isTimeRangeValid() = timeRange.length == 13

  private fun isLocationAndNotesValid() = locationAndNotes.length >= 2

  private fun isShiftColorValid() = isGood() || isBad()

  private fun isGood() = shiftColor == ShiftColor.GOLD.code

  private fun isBad() = shiftColor == ShiftColor.RED.code

  private fun setLocation(): String {
    val tempLocation = locationAndNotes.split(" ")[0].uppercase().replace("\"", "").trim(')', ';')
    if (tempLocation.length > 9) {
      return tempLocation.substring(0, 9)
    }

    if (locationAndNotes.contains("OTR", true)) {
      return "OTR"
    }

    return tempLocation
  }

  companion object {
    private enum class ShiftColor(val code: String) {
      RED("ff0000"),
      GOLD("ffcc00")
    }

    class DPMType private constructor(val type: String, val points: Int, val notes: String) {
      companion object {
        private val goodInstance = DPMType("Picked Up Block", 1, "Thanks!")
        fun good() = goodInstance
        fun bad(notes: String) = DPMType("DNS/Did Not Show", -10, notes)
      }
    }
  }
}
