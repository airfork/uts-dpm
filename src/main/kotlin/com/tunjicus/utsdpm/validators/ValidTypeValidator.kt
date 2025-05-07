package com.tunjicus.utsdpm.validators

import com.tunjicus.utsdpm.services.UserDpmService
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidTypeValidator : ConstraintValidator<ValidType, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    return if (value == null) false else UserDpmService.isValidType(value)
  }
}
