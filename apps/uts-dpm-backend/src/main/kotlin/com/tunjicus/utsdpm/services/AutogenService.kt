package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.AutogenDpm
import com.tunjicus.utsdpm.dtos.AutogenDpmDto
import com.tunjicus.utsdpm.dtos.AutogenWrapperDto
import com.tunjicus.utsdpm.entities.AutoSubmission
import com.tunjicus.utsdpm.exceptions.AutoSubmitAlreadyCalledException
import com.tunjicus.utsdpm.exceptions.AutogenException
import com.tunjicus.utsdpm.exceptions.NameNotFoundException
import com.tunjicus.utsdpm.helpers.formatSubmittedAt
import com.tunjicus.utsdpm.repositories.AutoSubmissionRepository
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AutogenService(
  private val autoSubmissionRepository: AutoSubmissionRepository,
  private val dpmService: DpmService,
  private val authService: AuthService
) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(AutogenService::class.java)
    private val CLIENT = OkHttpClient()
    private const val LOGIN_URL = "https://whentowork.com/cgi-bin/w2w.dll/login"

    private const val DAY_VIEW_LINK =
      "https://www7.whentowork.com/%s/mgrschedule?%s&lmi=1&view=Pos&Day=%s"

    // REGEX patterns
    private val SID = """SID=\w+""".toRegex()
    private val DLL_PATH = """data-w2w="/cgi-bin/w2wG.?\.dll""".toRegex()
    private val SHIFT_REGEX = """\wwl\("\d+",\d,"#\w+","[\w\d -:,~><^?@=|\\\[\]{}`]+;""".toRegex()
    private val BLOCK_LINE_REGEX = """sh\(\d+,"[\[\w+\]a-zA-Z ()]+","\d+""".toRegex()
    private val BLOCK_REGEX = """\[\w+]""".toRegex()
    private val BLOCK_COUNT_REGEX = """"\d+""".toRegex()
    private val autogenDpms = CopyOnWriteArrayList<AutogenDpmDto>()
  }

  @Value("\${app.w2wUser}") private lateinit var w2wUser: String
  @Value("\${app.w2wPass}") private lateinit var w2wPass: String

  fun autogenDtos(): AutogenWrapperDto {
    val today = TimeService.getTodayDate()
    val lastSubmission = autoSubmissionRepository.findMostRecent() ?: AutoSubmission.min()

    if (!today.isEqual(lastSubmission.submitted?.toLocalDate())) {
      return AutogenWrapperDto(dpms = autogen().map(AutogenDpmDto::from))
    }

    // should not happen
    if (autogenDpms.isEmpty()) autogenDpms.addAll(autogen().map(AutogenDpmDto::from))
    return AutogenWrapperDto(formatSubmittedAt(lastSubmission.submitted!!), autogenDpms)
  }

  fun autoSubmit() {
    val lastSubmission = autoSubmissionRepository.findMostRecent() ?: AutoSubmission.min()
    val today = TimeService.getTodayDate()

    if (today.isEqual(lastSubmission.submitted?.toLocalDate())) {
      throw AutoSubmitAlreadyCalledException()
    }

    val dpms = autogen()
    val currentUser = authService.getCurrentUser()
    for (dpm in dpms) {
      try {
        dpmService.newDpm(dpm, currentUser)
      } catch (ex: NameNotFoundException) {
        LOGGER.warn(ex.localizedMessage)
      }
    }

    autogenDpms.clear()
    autogenDpms.addAll(dpms.map(AutogenDpmDto::from))
    autoSubmissionRepository.save(AutoSubmission())
  }

  // Run daily
  // Delay one hour so repeated starts don't trigger event
  @Scheduled(fixedRate = 1000 * 60 * 60 * 24, initialDelay = 1000 * 60 * 60)
  @Transactional
  fun cleanupAutoSubmissionsTable() {
    LOGGER.info("Running auto submissions cleanup job")
    val monthAgo = LocalDateTime.now().minusMonths(1)
    val rowsDeleted = autoSubmissionRepository.deleteBySubmittedBefore(monthAgo)

    LOGGER.info("Cleanup job complete - $rowsDeleted entries removed")
  }

  private fun autogen(): List<AutogenDpm> {
    val loginBody = loginRequest()
    val sid =
      SID.find(loginBody)?.value ?: throw AutogenException("Failed to find SID in login body")
    var dllPath =
      DLL_PATH.find(loginBody)?.value ?: throw AutogenException("Failed to find DLL in login body")
    dllPath = dllPath.removePrefix("data-w2w=\"")

    val schedulePage = schedulePageRequest(sid, dllPath)
    val shifts = SHIFT_REGEX.findAll(schedulePage).map { it.value }
    val blocks = BLOCK_LINE_REGEX.findAll(schedulePage).map { it.value }

    return parseBlocks(shifts.toList(), blocks)
  }

  private fun loginRequest(): String {
    val request = Request.Builder().url(LOGIN_URL).post(createLoginFormData()).build()
    CLIENT.newCall(request).execute().use {
      return it.body?.string()
        ?: throw AutogenException("When2Work login request returned an empty body")
    }
  }

  private fun schedulePageRequest(sid: String, dllPath: String): String {
    val dayOfWeek = TimeService.getTodayZonedDateTime().dayOfWeek.value - 1
    val request =
      Request.Builder().url(String.format(DAY_VIEW_LINK, dllPath, sid, dayOfWeek)).build()
    CLIENT.newCall(request).execute().use {
      return it.body?.string()
        ?: throw AutogenException("Failed to get request body from schedule page request")
    }
  }

  private fun parseBlocks(shifts: List<String>, blocks: Sequence<String>): List<AutogenDpm> {
    var position = 0
    val dpmList = mutableListOf<AutogenDpm>()

    for (line in blocks) {
      // skip non [block] type entries
      if (!line.contains(BLOCK_REGEX)) {
        val split = line.split("\"")
        position += split.last().toInt()
        LOGGER.debug("Skipping non block type shift - $line")
        continue
      }

      val block =
        BLOCK_REGEX.find(line)?.value?.trim()
          ?: throw AutogenException("Failed to find block in line: $line")
      val blockShiftCount =
        BLOCK_COUNT_REGEX.find(line)?.value?.trim('"')?.toInt()
          ?: throw AutogenException("Failed to find shift count for block $block")

      val maxBlockPosition = position + blockShiftCount
      while (position < maxBlockPosition) {
        if (position >= shifts.size) {
          throw AutogenException(
            "Failed to parse all blocks. Please make sure that all blocks contain a value for location"
          )
        }

        val shift = shifts[position]
        // skip unassigned shifts
        if (shift[0] == 'e') {
          position++
          continue
        }

        // swl("952294753",2,"#000000","Brian Newman","959635624","07:00 - 14:20",
        // "7.33 hours","OFF");
        val shiftSplit = shift.split(',')
        val shiftColor = shiftSplit[2].trim('"').trimStart('#')
        val name = shiftSplit[3].trim('"')
        val timeRange = shiftSplit[5].trim('"')
        if (timeRange.length != 13) {
          LOGGER.info("Error getting time for $shiftSplit")
          position++
          continue
        }

        val startTime = timeRange.substring(0, 5).replace(":", "")
        val endTime = timeRange.substring(8).replace(":", "")
        val locationAndNotes = shiftSplit[7].trim('"').trim()
        if (locationAndNotes.length < 2) {
          LOGGER.info("Line has no location - $shiftSplit")
          position++
          continue
        }

        var location = locationAndNotes.split(" ")[0].uppercase().replace("\"", "").trim(')', ';')
        if (location.length > 9) {
          location = location.substring(0, 9)
        }

        if (locationAndNotes.contains("OTR", true)) {
          location = "OTR"
        }

        // only looking for red, FF0000, and gold, ffcc00
        if (shiftColor.lowercase() != "ff0000" && shiftColor.lowercase() != "ffcc00") {
          position++
          continue
        }

        val type: String
        val points: Int
        val notes: String

        // good DPM
        if (shiftColor.lowercase() == "ffcc00") {
          type = "Picked Up Block"
          points = 1
          notes = "Thanks!"
        }

        // bad DPM
        else {
          type = "DNS/Did Not Show"
          points = -10
          val dnsIndex = locationAndNotes.indexOf("DNS", ignoreCase = true)
          notes =
            if (dnsIndex == -1) ""
            else {
              locationAndNotes
                .substring(dnsIndex + 3)
                .replace("(", "")
                .replace(")", "")
                .replace("\"", "")
                .trim()
            }
        }

        dpmList.add(AutogenDpm(name, block, startTime, endTime, location, type, points, notes))
        position++
      }
    }

    return dpmList
  }

  private fun createLoginFormData(): RequestBody =
    MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("UserId1", w2wUser)
      .addFormDataPart("Password1", w2wPass)
      .addFormDataPart("Launch", "")
      .addFormDataPart("LaunchParams", "")
      .addFormDataPart("Submit1", "Please Wait...")
      .addFormDataPart("captca_required", "false")
      .addFormDataPart("name", "signin")
      .build()
}
