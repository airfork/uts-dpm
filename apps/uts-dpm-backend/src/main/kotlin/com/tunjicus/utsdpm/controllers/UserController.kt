package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
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
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content =
            [Content(array = ArraySchema(schema = Schema(implementation = UsernameDto::class)))]
        )]
  )
  @GetMapping("/names")
  fun getAllUserNames(): Collection<UsernameDto> = userService.getAllUserNames()

  @Operation(
    summary = "Gets info on the user with the passed in id",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content = [Content(schema = Schema(implementation = GetUserDetailDto::class))]
        ),
        ApiResponse(
          responseCode = "404",
          description = "Failed to to find user with passed in id",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @GetMapping("/{id}")
  fun getUser(@PathVariable id: Int): GetUserDetailDto = userService.findById(id)

  @Operation(
    summary = "Updates user information",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful update",
        ),
        ApiResponse(
          responseCode = "400",
          description = "Bad request, likely due to being unable to find the manager",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "404",
          description = "Failed to find use with passed in id",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PatchMapping("/{id}")
  fun updateUser(@PathVariable id: Int, @RequestBody dto: UserDetailDto) =
    userService.updateUser(dto, id)
}
