package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class CreateUserDto {
  @NotBlank(message = "email cannot be empty")
  @Email(message = "email must be a valid email address")
  var email: String? = null

  @NotBlank(message = "first name cannot be empty") var firstname: String? = null

  @NotBlank(message = "last name cannot be empty") var lastname: String? = null

  var manager: String? = null

  @NotNull(message = "managerId cannot be null") var managerId: Int? = null

  @NotBlank(message = "role cannot be empty") var role: String? = null

  var fullTime: Boolean = false
}
