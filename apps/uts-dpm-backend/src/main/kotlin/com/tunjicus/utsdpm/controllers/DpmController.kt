package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.dtos.ApprovalDpmDto
import com.tunjicus.utsdpm.dtos.HomeDpmDto
import com.tunjicus.utsdpm.dtos.PatchDpmDto
import com.tunjicus.utsdpm.dtos.PostDpmDto
import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.exceptions.ExceptionResponses
import com.tunjicus.utsdpm.services.DpmService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@Validated
@RequestMapping(value = ["/api/dpms"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "DPM", description = "Endpoints for managing DPM data")
class DpmController(private val dpmService: DpmService) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(DpmController::class.java)
  }

  @Operation(
    summary = "Create a dpm for a user",
    description = "Creates a dpm for a user and runs validations on the fields",
    responses = [
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
        responseCode = "422",
        description = "Unable to find user the DPM is meant for",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
      )
    ]
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun new(@RequestBody @Valid dpmDto: PostDpmDto) = dpmService.newDpm(dpmDto)

  @Operation(
    summary = "Gets the current user's dpms for the last sixth months",
    responses = [ApiResponse(
      responseCode = "200",
      description = "Successful request",
      content = [Content(array = ArraySchema(schema = Schema(implementation = HomeDpmDto::class)))]
    )]
  )
  @GetMapping
  fun getCurrentDpms(): Collection<HomeDpmDto> = dpmService.getCurrentDpms()

  @Operation(
    summary = "Gets all the unapproved dpms",
    description = "Unapproved dpms are ones that have approved set to null or false (legacy) and have ignored set to null or false",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Successful request",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ApprovalDpmDto::class)))]
      )
    ]
  )
  @GetMapping("/approvals")
  fun getUnapprovedDpms(): Collection<ApprovalDpmDto> = dpmService.getUnapprovedDpms()

  @Operation(
    summary = "Updates dpm fields",
    description = "Updates the following fields: points, approved, and ignored",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Updates was successful"
      ),
      ApiResponse(
        responseCode = "404",
        description = "Failed to find a dpm with the id in the path variable",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
      )
    ]
  )
  @PatchMapping("/{id}")
  fun updateDpm(@PathVariable id: Int, @RequestBody dto: PatchDpmDto) = dpmService.updateDpm(id, dto)
}
