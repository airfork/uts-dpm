package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.entities.Dpm
import com.tunjicus.utsdpm.entities.User
import com.tunjicus.utsdpm.enums.RoleName
import com.tunjicus.utsdpm.exceptions.InvalidDataGenDateException
import com.tunjicus.utsdpm.helpers.*
import com.tunjicus.utsdpm.repositories.DpmRepository
import com.tunjicus.utsdpm.repositories.UserRepository
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataGenService(
  private val dpmRepository: DpmRepository,
  private val userRepository: UserRepository,
  private val authService: AuthService
) {
  fun generateDpmSpreadSheet(startDate: String?, endDate: String?): String {
    LOGGER.info("Startdate: $startDate, EndDate: $endDate")
    val (start, end) = getStartAndEndDates(startDate, endDate)
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("DPMs")

    val headerStyle = workbook.createCellStyle()
    val headerFont = workbook.createFont()

    headerFont.fontName = "Arial"
    headerFont.fontHeightInPoints = 14.toShort()
    headerFont.bold = true
    headerStyle.setFont(headerFont)
    setColumnWidthsAndHeader(sheet, headerStyle, DPM_HEADERS)

    val style = workbook.createCellStyle()
    style.wrapText = true

    val cellFont = workbook.createFont()
    cellFont.fontName = "Arial"
    cellFont.fontHeightInPoints = 12.toShort()
    style.setFont(cellFont)

    val currentUser = authService.getCurrentUser()
    var dpms = dpmRepository.findAllByCreatedAfterAndCreatedBeforeOrderByCreatedDesc(start, end)
    if (currentUser.role?.roleName == RoleName.MANAGER) {
      dpms = dpms.filter { it.user?.manager?.id == currentUser.id }
    }

    for ((index, dpm) in dpms.withIndex()) {
      setDpmRows(sheet, dpm, index, style)
    }

    val tempFile = File.createTempFile("dpms", ".xlsx")
    val fileLocation = tempFile.absolutePath

    val outputStream = FileOutputStream(fileLocation)
    workbook.write(outputStream)
    workbook.close()

    return fileLocation
  }

  fun generateUserSpreadSheet(): String {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Users")

    val headerStyle = workbook.createCellStyle()
    val headerFont = workbook.createFont()

    headerFont.fontName = "Arial"
    headerFont.fontHeightInPoints = 14.toShort()
    headerFont.bold = true
    headerStyle.setFont(headerFont)
    setColumnWidthsAndHeader(sheet, headerStyle, USER_HEADERS)

    val style = workbook.createCellStyle()
    style.wrapText = true

    val cellFont = workbook.createFont()
    cellFont.fontName = "Arial"
    cellFont.fontHeightInPoints = 12.toShort()
    style.setFont(cellFont)

    val currentUser = authService.getCurrentUser()
    var users = userRepository.findAllSorted()
    if (currentUser.role?.roleName == RoleName.MANAGER) {
      users = users.filter { it.manager?.id == currentUser.id }
    }

    for ((index, user) in users.withIndex()) {
      setUserRows(sheet, user, index, style)
    }

    val tempFile = File.createTempFile("users", ".xlsx")
    val fileLocation = tempFile.absolutePath

    val outputStream = FileOutputStream(fileLocation)
    workbook.write(outputStream)
    workbook.close()

    return fileLocation
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(DataGenService::class.java)
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    private val MIN_TIMESTAMP = ZonedDateTime.now().minusYears(3000)
    private val MAX_TIMESTAMP = ZonedDateTime.now().plusYears(3000)
    private const val SMALL_WIDTH = 4000
    private const val LARGE_WIDTH = 7000
    private const val MEDIUM_WIDTH = 5000
    private const val EXTRA_LARGE_WIDTH = 14000
    private const val NOTES_WIDTH = 17000

    private val FIRST_NAME_HEADER = Pair("First Name", LARGE_WIDTH)
    private val LAST_NAME_HEADER = Pair("Last Name", LARGE_WIDTH)
    private val POINTS_HEADER = Pair("Points", SMALL_WIDTH)

    private val DPM_HEADERS =
      listOf(
        FIRST_NAME_HEADER,
        LAST_NAME_HEADER,
        Pair("Block", MEDIUM_WIDTH),
        Pair("Location", MEDIUM_WIDTH),
        Pair("Start Time", MEDIUM_WIDTH),
        Pair("End Time", MEDIUM_WIDTH),
        Pair("Date", MEDIUM_WIDTH),
        Pair("Type", EXTRA_LARGE_WIDTH),
        POINTS_HEADER,
        Pair("Notes", NOTES_WIDTH),
        Pair("Status", LARGE_WIDTH + 2000),
        Pair("Created", LARGE_WIDTH),
        Pair("Created By", LARGE_WIDTH),
      )

    private val USER_HEADERS =
      listOf(LAST_NAME_HEADER, FIRST_NAME_HEADER, POINTS_HEADER, Pair("Manager", LARGE_WIDTH))

    fun setColumnWidthsAndHeader(
      sheet: Sheet,
      headerStyle: CellStyle,
      headers: List<Pair<String, Int>>
    ) {
      val header = sheet.createRow(0)

      for ((index, value) in headers.withIndex()) {
        sheet.setColumnWidth(index, value.second)
        val headerCell = header.createCell(index)
        headerCell.setCellValue(value.first)
        headerCell.cellStyle = headerStyle
      }
    }

    fun setUserRows(sheet: Sheet, user: User, index: Int, style: XSSFCellStyle) {
      val row = sheet.createRow(index + 1)
      var cellIndex = 0

      var cell = row.createCell(cellIndex++)
      cell.setCellValue(user.lastname)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(user.firstname)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(user.points.toString())
      cell.cellStyle = style

      cell = row.createCell(cellIndex)
      cell.setCellValue("${user.manager?.firstname} ${user.manager?.lastname}")
      cell.cellStyle = style
    }

    fun setDpmRows(sheet: Sheet, dpm: Dpm, index: Int, style: XSSFCellStyle) {
      val row = sheet.createRow(index + 1)
      var cellIndex = 0

      var cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.user?.firstname)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.user?.lastname)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.block)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.location)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(formatOutboundDpmTime(dpm.startTime))
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(formatOutboundDpmTime(dpm.endTime))
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(formatOutboundDpmDate(dpm.date))
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.dpmType)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.points?.toString())
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(dpm.notes)
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(generateDpmStatusMessage(dpm.approved!!, dpm.ignored!!))
      cell.cellStyle = style

      cell = row.createCell(cellIndex++)
      cell.setCellValue(formatCreatedAtExcel(dpm.created))
      cell.cellStyle = style

      cell = row.createCell(cellIndex)
      cell.setCellValue("${dpm.createdUser?.firstname} ${dpm.createdUser?.lastname}".trim())
      cell.cellStyle = style
    }

    private fun getStartAndEndDates(
      startDate: String?,
      endDate: String?
    ): Pair<ZonedDateTime, ZonedDateTime> {
      if (startDate == null && endDate == null) {
        LOGGER.info("Generating spreadsheet for all DPMs")
        return Pair(MIN_TIMESTAMP, MAX_TIMESTAMP)
      }

      if (startDate == null) {
        val end =
          formatDateOrNull(endDate!!, DATE_FORMAT)?.plusDays(1)
            ?: throw InvalidDataGenDateException(
              "endDate query param is not in the correct format - MM-dd-yyyy"
            )

        LOGGER.info("Generating spreadsheet with no startDate and endDate of $end")
        return Pair(MIN_TIMESTAMP, end)
      }

      if (endDate == null) {
        val start =
          formatDateOrNull(startDate, DATE_FORMAT)
            ?: throw InvalidDataGenDateException(
              "startDate query param is not in the correct format - MM-dd-yyyy"
            )

        LOGGER.info("Generating spreadsheet with startDate of $start and no endDate")
        return Pair(start, MAX_TIMESTAMP)
      }

      val start =
        formatDateOrNull(startDate, DATE_FORMAT)
          ?: throw InvalidDataGenDateException(
            "startDate query param is not in the correct format - MM-dd-yyyy"
          )

      val end =
        formatDateOrNull(endDate, DATE_FORMAT)?.plusDays(1)
          ?: throw InvalidDataGenDateException(
            "endDate query param is not in the correct format - MM-dd-yyyy"
          )

      LOGGER.info("Generating spreadsheet with startDate of $start and endDate of $end")
      return Pair(start, end)
    }
  }
}
