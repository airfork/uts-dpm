package com.tunjicus.utsdpm.controllers

import com.tunjicus.utsdpm.exceptions.ExceptionResponse
import com.tunjicus.utsdpm.services.DataGenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "DataGen", description = "Routes for generating excel data")
@RequestMapping(value = ["/api/datagen"])
class DataGenController(private val dataGenService: DataGenService) {
  @Operation(
    summary = "Generates a spreadsheet with DPM data",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content = [Content(mediaType = "Application/vnd.ms-excel")]
        ),
        ApiResponse(
          responseCode = "400",
          description = "Failed to parse the start and/or end date query param correctly",
          content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
        )]
  )
  @GetMapping("/dpms")
  fun generateDpmData(
    @RequestParam startDate: String?,
    @RequestParam endDate: String?,
  ): ResponseEntity<ByteArrayResource> =
    returnExcelFile(File(dataGenService.generateDpmSpreadSheet(startDate, endDate)), "DPMs")

  @Operation(
    summary = "Generates a spreadsheet with user data",
    responses =
      [
        ApiResponse(
          responseCode = "200",
          description = "Successful request",
          content = [Content(mediaType = "Application/vnd.ms-excel")]
        )]
  )
  @GetMapping("/users")
  fun generateUserData(): ResponseEntity<ByteArrayResource> =
    returnExcelFile(File(dataGenService.generateUserSpreadSheet()), "Users")

  companion object {
    private val LOGGER = LoggerFactory.getLogger(DataGenController::class.java)

    private fun returnExcelFile(file: File, filename: String): ResponseEntity<ByteArrayResource> {
      val header = HttpHeaders()
      header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename.xlsx")
      header.add("Cache-Control", "no-cache, no-store, must-revalidate")
      header.add("Pragma", "no-cache")
      header.add("Expires", "0")

      val path: Path = Paths.get(file.absolutePath)
      val resource = ByteArrayResource(Files.readAllBytes(path))

      return ResponseEntity.ok()
        .headers(header)
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource)
    }
  }
}
