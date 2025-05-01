package com.tunjicus.utsdpm.helpers

import com.tunjicus.utsdpm.models.AutogenDpm

class BlockComparator : Comparator<AutogenDpm> {
  override fun compare(o1: AutogenDpm, o2: AutogenDpm): Int {
    val contentA = extractBracketContent(o1.block)
    val contentB = extractBracketContent(o2.block)

    val isANumeric = contentA.all { it.isDigit() }
    val isBNumeric = contentB.all { it.isDigit() }

    return when {
      // Both are numeric - compare as numbers
      isANumeric && isBNumeric -> contentA.toInt().compareTo(contentB.toInt())

      // A is numeric, B is not - A comes first
      isANumeric && !isBNumeric -> -1

      // B is numeric, A is not - B comes first
      !isANumeric && isBNumeric -> 1

      // Neither is numeric - compare alphabetically
      else -> contentA.compareTo(contentB)
    }
  }

  fun extractBracketContent(value: String): String {
    val openBracket = value.indexOf('[')
    val closeBracket = value.indexOf(']')

    if (openBracket != -1 && closeBracket != -1 && openBracket < closeBracket) {
      return value.substring(openBracket + 1, closeBracket)
    }

    return value // Return original if brackets aren't found
  }
}
