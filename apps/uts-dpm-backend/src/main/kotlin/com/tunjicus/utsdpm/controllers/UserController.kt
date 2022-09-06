package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@Tag(name = "Users", description = "Routes for managing user data")
@RequestMapping(value = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(private val userService: UserService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(UserController::class.java)
  }

  @Operation(
    summary = "Gets the names of all the users",
    responses = [ApiResponse(
      responseCode = "200",
      description = "Successful request",
      content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))]
    )]
  )
  @GetMapping("/names")
  fun getAllUserNames(): Collection<String> = userService.getAllUserNames()
}
