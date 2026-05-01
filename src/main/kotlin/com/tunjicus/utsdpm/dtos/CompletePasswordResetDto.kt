package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class CompletePasswordResetDto {
  @field:NotBlank(message = "token cannot be blank") var token: String? = null

  @field:NotBlank(message = "newPassword cannot be blank")
  @field:Length(min = 8, message = "newPassword must be at least 8 characters")
  var newPassword: String? = null

  @field:NotBlank(message = "confirmPassword cannot be blank")
  var confirmPassword: String? = null
}
