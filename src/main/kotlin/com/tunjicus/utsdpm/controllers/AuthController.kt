package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.ChangePasswordDto
import com.tunjicus.utsdpm.dtos.ChangeRequiredDto
import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.dtos.LoginResponseDto
import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.exceptions.ExceptionResponses
import com.tunjicus.utsdpm.exceptions.SecurityExceptionResponse
import com.tunjicus.utsdpm.services.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping(value = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Auth", description = "Holds the authentication endpoints")
@SecurityRequirements
class AuthController(private val authService: AuthService) {

  @Operation(
    summary = "Logins in the user",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful login",
          content = [Content(schema = Schema(implementation = LoginResponseDto::class))]
        ),
        ApiResponse(
          responseCode = "401",
          description = "Authentication failed for the username and password entered"
        )]
  )
  @PostMapping("/login")
  fun authenticate(@RequestBody dto: LoginDto) = authService.authenticateUser(dto)

  @Operation(
    summary = "Check to see if the user needs to change their password",
    responses =
      [
        ApiResponse(responseCode = "200", description = "Response successfully generated"),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
      ]
  )
  @GetMapping("/changeCheck")
  fun checkPasswordChange(): ChangeRequiredDto = authService.changeRequired()

  @Operation(
    summary = "Change the current user's password",
    responses =
      [
        ApiResponse(responseCode = "200", description = "Password was successfully changed"),
        ApiResponse(
          responseCode = "400",
          description = "Validation failed on the request body",
          content = [Content(schema = Schema(implementation = ExceptionResponses::class))]
        ),
        ApiResponse(
          responseCode = "401",
          description =
            "Unauthorized. Either login is required or the currentPassword was incorrect",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "422",
          description =
            "Validation checks failed on the password fields themselves. I.e. the new password matches the current one",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),
      ]
  )
  @PatchMapping("/changePassword")
  fun changePassword(@Valid @RequestBody dto: ChangePasswordDto) = authService.changePassword(dto)
}
