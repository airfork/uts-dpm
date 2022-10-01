package com.tunjicus.utsdpm.dtos

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreateUserDto {
  @NotBlank(message = "email cannot be empty")
  var email: String? = null

  @NotBlank(message = "first name cannot be empty")
  var firstname: String? = null

  @NotBlank(message = "last name cannot be empty")
  var lastname: String? = null

  @NotBlank(message = "manager cannot be empty")
  var manager: String? = null

  @NotBlank(message = "role cannot be empty")
  var role: String? = null

  var fullTime: Boolean = false
}
