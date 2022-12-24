package com.tunjicus.utsdpm.validators

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidDateFormatValidator : ConstraintValidator<ValidDateFormat, String> {
  private var format: String? = null

  override fun initialize(constraintAnnotation: ValidDateFormat?) {
    format = constraintAnnotation?.value
  }

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return false

    val formatter: DateTimeFormatter
    try {
      formatter = DateTimeFormatter.ofPattern(format)
    } catch (_: IllegalArgumentException) {
      return false
    }

    try {
      formatter.parse(value)
    } catch (_: DateTimeParseException) {
      return false
    }

    return true
  }
}
