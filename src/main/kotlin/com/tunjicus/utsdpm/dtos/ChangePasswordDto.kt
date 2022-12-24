package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class ChangePasswordDto {
  @NotBlank(message = "current password cannot be blank") var currentPassword: String? = null

  @NotBlank(message = "new password cannot be blank")
  @Length(min = 8, message = "new password must be at least 8 characters long")
  var newPassword: String? = null

  @NotBlank(message = "confirm password cannot be blank") var confirmPassword: String? = null
}
