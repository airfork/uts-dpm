package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.*
import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.exceptions.ExceptionResponses
import com.tunjicus.utsdpm.exceptions.SecurityExceptionResponse
import com.tunjicus.utsdpm.services.DpmService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping(value = ["/api/dpms"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "DPMs", description = "Endpoints for managing DPM data")
class DpmController(private val dpmService: DpmService) {

  @Operation(
    summary = "Create a dpm for a user",
    description = "Creates a dpm for a user and runs validations on the fields",
    responses =
      [
        ApiResponse(
          responseCode = "201",
          description = "DPM was successfully created",
        ),
        ApiResponse(
          responseCode = "400",
          description = "Unable to validate JSON object",
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
          description = "Unable to find user the DPM is meant for",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'MANAGER', 'SUPERVISOR')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun new(@RequestBody @Valid dpmDto: PostDpmDto) = dpmService.newDpm(dpmDto)

  @Operation(
    summary = "Gets the current user's dpms for the last sixth months",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content =
            [Content(array = ArraySchema(schema = Schema(implementation = HomeDpmDto::class)))]
        ),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized, need to login",
          content = [Content(schema = Schema(implementation = SecurityExceptionResponse::class))]
        )]
  )
  @GetMapping("/current")
  fun getCurrentDpms(): Collection<HomeDpmDto> = dpmService.getCurrentDpms()

  @Operation(
    summary = "Gets all the unapproved dpms",
    description =
      "Unapproved dpms are ones that have approved set to null or false (legacy) and have ignored set to null or false." +
        "Admins can view all unapproved dpms, but managers can only view dpms for people they manage",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content =
            [Content(array = ArraySchema(schema = Schema(implementation = ApprovalDpmDto::class)))]
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
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @GetMapping("/approvals")
  fun getUnapprovedDpms(): Collection<ApprovalDpmDto> = dpmService.getUnapprovedDpms()

  @Operation(
    summary = "Updates dpm fields",
    description = "Updates the following fields: points, approved, and ignored",
    responses =
      [
        ApiResponse(responseCode = "200", description = "Updates were successful"),
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
          description = "Failed to find a dpm with the id in the path variable",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PatchMapping("/{id}")
  fun updateDpm(
    @Parameter(description = "The id of the DPM") @PathVariable id: Int,
    @RequestBody dto: PatchDpmDto
  ) = dpmService.updateDpm(id, dto)

  @Operation(
    summary = "Get all the dpms for the user",
    responses =
      [
        ApiResponse(responseCode = "200", description = "Request completed successfully"),
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
          description = "Failed to find a user with the id in the path variable",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/user/{id}")
  fun getAll(
    @Parameter(description = "The page number for pagination")
    @RequestParam(defaultValue = "0")
    page: Int,
    @Parameter(description = "The page size for pagination")
    @RequestParam(defaultValue = "10")
    size: Int,
    @Parameter(description = "The id of user") @PathVariable id: Int
  ): Page<DpmDetailDto> = dpmService.getAll(id, page, size)
}
