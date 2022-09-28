package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.LoginDto
import com.tunjicus.utsdpm.dtos.LoginResponseDto
import com.tunjicus.utsdpm.services.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Auth", description = "Holds the authentication endpoints")
@SecurityRequirements
class AuthController(private val authService: AuthService) {

  @Operation(
    summary = "Gets the names of all the users",
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
}
