package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.services.AutogenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/autogen"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(
  name = "Autogen",
  description = "Endpoint for generating and submitting DPM data from When2Work"
)
class AutogenController(private val autogenService: AutogenService) {

  @Operation(
    summary = "Autogenerate DPM data",
    responses =
    [
      ApiResponse(
        responseCode = "200",
        description = "DPMs were generated successfully",
      ),
      ApiResponse(
        responseCode = "500",
        description = "Something went wrong trying to generate the DPMs",
        content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
      )]
  )
  @GetMapping fun autogen() = autogenService.autogenDtos()
}
