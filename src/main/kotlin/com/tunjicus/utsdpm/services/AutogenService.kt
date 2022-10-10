package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.dtos.AutogenDpm
import com.tunjicus.utsdpm.dtos.AutogenDpmDto
import com.tunjicus.utsdpm.dtos.AutogenWrapperDto
import com.tunjicus.utsdpm.entities.AutoSubmission
import com.tunjicus.utsdpm.exceptions.AutoSubmitAlreadyCalledException
import com.tunjicus.utsdpm.exceptions.AutogenException
import com.tunjicus.utsdpm.exceptions.NameNotFoundException
import com.tunjicus.utsdpm.helpers.FormatHelpers
import com.tunjicus.utsdpm.models.ShiftInfo
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

@Service
class AutogenService(
  private val autoSubmissionRepository: AutoSubmissionRepository,
  private val dpmService: DpmService,
  private val authService: AuthService
) {
  @Value("\${app.w2wUser}") private lateinit var w2wUser: String
  @Value("\${app.w2wPass}") private lateinit var w2wPass: String

  fun autogenDtos(): AutogenWrapperDto {
    if (!alreadyCalledToday()) {
      return AutogenWrapperDto(dpms = autogen().map(AutogenDpmDto::from))
    }

    // handle case where app restarts and DPMs have already been submitted
    if (autogenDpms.isEmpty()) autogenDpms.addAll(autogen().map(AutogenDpmDto::from))
    return AutogenWrapperDto(FormatHelpers.submittedAt(lastSubmission().submitted), autogenDpms)
  }

  fun autoSubmit() {
    if (alreadyCalledToday()) {
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
    val monthAgo = TimeService.getTodayZonedDateTime().minusMonths(1)
    val rowsDeleted = autoSubmissionRepository.deleteBySubmittedBefore(monthAgo)

    LOGGER.info("Cleanup job complete - $rowsDeleted entries removed")
  }

  private fun alreadyCalledToday(): Boolean =
    TimeService.getTodayDate().isEqual(lastSubmission().submitted.toLocalDate())

  private fun lastSubmission(): AutoSubmission =
    autoSubmissionRepository.findMostRecent() ?: AutoSubmission.min()

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

        val shiftSplit = shift.split(',')
        val shiftInfo = ShiftInfo(block, shiftSplit)

        val (valid, message) = shiftInfo.isValid()
        if (!valid) {
          if (message.isNotBlank()) {
            LOGGER.info("$message - $shiftSplit")
          }
          position++
          continue
        }

        dpmList.add(AutogenDpm(shiftInfo))
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

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AutogenService::class.java)
    private val CLIENT = OkHttpClient()
    private const val LOGIN_URL = "https://whentowork.com/cgi-bin/w2w.dll/login"

    private const val DAY_VIEW_LINK =
      "https://www7.whentowork.com/%s/mgrschedule?%s&lmi=1&view=Pos&Day=%s"

    // REGEX patterns
    private val SID = """SID=\w+""".toRegex()
    private val DLL_PATH = """data-w2w="/cgi-bin/w2wG.?\.dll""".toRegex()
    private val SHIFT_REGEX = """\wwl\("\d+",\d,"#\w+","[\w -:,~><^?@=|\\\[\]{}`]+;""".toRegex()
    private val BLOCK_LINE_REGEX = """sh\(\d+,"[\[\w+\]a-zA-Z ()]+","\d+""".toRegex()
    private val BLOCK_REGEX = """\[\w+]""".toRegex()
    private val BLOCK_COUNT_REGEX = """"\d+""".toRegex()
    private val autogenDpms = CopyOnWriteArrayList<AutogenDpmDto>()
  }
}
