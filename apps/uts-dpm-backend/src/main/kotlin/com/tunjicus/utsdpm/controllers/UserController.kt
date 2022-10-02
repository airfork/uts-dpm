package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.CreateUserDto
import com.tunjicus.utsdpm.dtos.GetUserDetailDto
import com.tunjicus.utsdpm.dtos.UserDetailDto
import com.tunjicus.utsdpm.dtos.UsernameDto
import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.exceptions.ExceptionResponses
import com.tunjicus.utsdpm.exceptions.SecurityExceptionResponse
import com.tunjicus.utsdpm.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import javax.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@Tag(name = "Users", description = "Routes for managing user data")
@RequestMapping(value = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(private val userService: UserService) {

  @Operation(
    summary = "Gets the names of all the users",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content =
            [Content(array = ArraySchema(schema = Schema(implementation = UsernameDto::class)))]
        ),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
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
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "403",
          description = "User does not have the correct permissions to perform this action",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "404",
          description = "Failed to to find user with passed in id",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PreAuthorize("hasRole('ADMIN')")
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
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "403",
          description = "User does not have the correct permissions to perform this action",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "404",
          description = "Failed to find user with passed in id",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  fun updateUser(@PathVariable id: Int, @RequestBody dto: UserDetailDto) =
    userService.updateUser(dto, id)

  @Operation(
    summary = "Gets the names of all of the managers",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Managers retrieved successfully",
          content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))]
        ),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "403",
          description = "User does not have the correct permissions to perform this action",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
      ]
  )
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/managers")
  fun getManagers() = userService.getManagers()

  @Operation(
    summary = "Creates a user",
    responses =
      [
        ApiResponse(
          responseCode = "201",
          description = "User was successfully created",
          content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))]
        ),
        ApiResponse(
          responseCode = "400",
          description = "Failed to validate input object",
          content = [Content(schema = Schema(implementation = ExceptionResponses::class))]
        ),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "403",
          description = "User does not have the correct permissions to perform this action",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "422",
          description =
            "Something went wrong with the data in the input object. Likely the manager doesn't exist " +
              "or a user with that username already exists",
          content = [Content(schema = Schema(implementation = ExceptionResponses::class))]
        ),
      ]
  )
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  fun createUser(@Valid @RequestBody dto: CreateUserDto) = userService.createUser(dto)

  @Operation(
    summary = "Resets the points balance of all part-timers and ignores all of their approved DPMs",
    responses =
      [
        ApiResponse(responseCode = "200", description = "Reset was completed successfully"),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
        ApiResponse(
          responseCode = "403",
          description = "User does not have the correct permissions to perform this action",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        ),
      ]
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/points/reset")
  fun resetPointBalances() = userService.resetPointBalances()

  @Operation(
    summary = "Deletes the specified user",
    responses =
    [
      ApiResponse(responseCode = "200", description = "User was successfully deleted"),
      ApiResponse(
        responseCode = "400",
        description = "Bad request, likely due to trying to self-delete",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized, need to login",
        content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "User does not have the correct permissions to perform this action",
        content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
      ),
    ]
  )
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  fun delete(@PathVariable id: Int) = userService.deleteUser(id)
}
