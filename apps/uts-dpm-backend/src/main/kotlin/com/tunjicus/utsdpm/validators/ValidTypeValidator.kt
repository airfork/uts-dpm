package com.tunjicus.utsdpm.validators

import com.tunjicus.utsdpm.services.DpmService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ValidTypeValidator : ConstraintValidator<ValidType, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    return if (value == null) false else DpmService.isValidType(value)
  }
}
