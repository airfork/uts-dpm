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
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
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
    LOGGER.info("Start date: $startDate, End date: $endDate")
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("DPMs")
    setColumnWidthsAndHeader(sheet, createHeaderStyle(workbook), DPM_HEADERS)

    val dpms = getDpmsInRange(startDate, endDate)
    val cellStyle = createCellStyle(workbook)
    for ((index, dpm) in dpms.withIndex()) {
      setDpmRows(sheet.createRow(index + 1), dpm, cellStyle)
    }

    return saveWorkbook(workbook, "dpms")
  }

  fun generateUserSpreadSheet(): String {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Users")
    setColumnWidthsAndHeader(sheet, createHeaderStyle(workbook), USER_HEADERS)

    val users = getUsers()
    val cellStyle = createCellStyle(workbook)
    for ((index, user) in users.withIndex()) {
      setUserRows(sheet.createRow(index + 1), user, cellStyle)
    }

    return saveWorkbook(workbook, "users")
  }

  private fun getDpmsInRange(startDate: String?, endDate: String?): Collection<Dpm> {
    val (start, end) = getStartAndEndDates(startDate, endDate)
    val currentUser = authService.getCurrentUser()
    var dpms = dpmRepository.findAllByCreatedAfterAndCreatedBeforeOrderByCreatedDesc(start, end)

    if (currentUser.hasAnyRole(RoleName.MANAGER)) {
      dpms = dpms.filter { it.user?.manager?.id == currentUser.id }
    }

    return dpms
  }

  private fun getUsers(): Collection<User> {
    val currentUser = authService.getCurrentUser()
    var users = userRepository.findAllSorted()

    if (currentUser.hasAnyRole(RoleName.MANAGER)) {
      users = users.filter { it.manager?.id == currentUser.id }
    }

    return users
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

    private fun saveWorkbook(workbook: XSSFWorkbook, prefix: String): String {
      val tempFile = File.createTempFile(prefix, ".xlsx")
      val fileLocation = tempFile.absolutePath

      val outputStream = FileOutputStream(fileLocation)
      workbook.write(outputStream)
      workbook.close()
      return fileLocation
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
      val headerFont = workbook.createFont()
      headerFont.fontName = "Arial"
      headerFont.fontHeightInPoints = 14.toShort()
      headerFont.bold = true

      val headerStyle = workbook.createCellStyle()
      headerStyle.setFont(headerFont)
      return headerStyle
    }

    private fun createCellStyle(workbook: XSSFWorkbook): XSSFCellStyle {
      val cellFont = workbook.createFont()
      cellFont.fontName = "Arial"
      cellFont.fontHeightInPoints = 12.toShort()

      val style = workbook.createCellStyle()
      style.wrapText = true
      style.setFont(cellFont)
      return style
    }

    private fun setColumnWidthsAndHeader(
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

    private fun setRow(row: Row, style: XSSFCellStyle, contents: List<String?>) {
      var cell: Cell
      for ((index, value) in contents.withIndex()) {
        cell = row.createCell(index)
        cell.setCellValue(value)
        cell.cellStyle = style
      }
    }

    private fun setUserRows(row: Row, user: User, style: XSSFCellStyle) {
      val rowContents =
        listOf(
          user.lastname,
          user.firstname,
          user.points?.toString(),
          "${user.manager?.firstname} ${user.manager?.lastname}"
        )

      setRow(row, style, rowContents)
    }

    private fun setDpmRows(row: Row, dpm: Dpm, style: XSSFCellStyle) {
      val rowContents =
        listOf(
          dpm.user?.firstname,
          dpm.user?.lastname,
          dpm.block,
          dpm.location,
          formatOutboundDpmTime(dpm.startTime),
          formatOutboundDpmTime(dpm.endTime),
          formatOutboundDpmDate(dpm.date),
          dpm.dpmType,
          dpm.points?.toString(),
          dpm.notes,
          generateDpmStatusMessage(dpm.approved!!, dpm.ignored!!),
          formatCreatedAtExcel(dpm.created),
          "${dpm.createdUser?.firstname} ${dpm.createdUser?.lastname}".trim()
        )

      setRow(row, style, rowContents)
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
