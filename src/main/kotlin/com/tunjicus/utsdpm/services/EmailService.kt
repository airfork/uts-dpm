package com.tunjicus.utsdpm.services

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import com.mailgun.model.message.Message
import com.tunjicus.utsdpm.emailModels.DpmReceived
import com.tunjicus.utsdpm.emailModels.PointsBalance
import com.tunjicus.utsdpm.emailModels.Reset
import com.tunjicus.utsdpm.emailModels.Welcome
import com.tunjicus.utsdpm.helpers.Constants
import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import java.util.concurrent.CompletableFuture

@Service
class EmailService(private val fmConfiguration: Configuration, private val constants: Constants) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(EmailService::class.java)
    private const val DOMAIN = "utsdpm.com"
    private const val EMAIL_FROM = "DPM@utsdpm.com"
  }

  private val mailgunClient: MailgunMessagesApi =
    MailgunClient.config(constants.mailgunKey())
      .createApi(MailgunMessagesApi::class.java)

  @Async
  fun sendPointsEmail(to: String, model: PointsBalance): CompletableFuture<Void> =
    sendEmail(to, generatePointsBalanceEmail(model), "DPM Points Balance")

  @Async
  fun sendDpmEmail(to: String, model: DpmReceived): CompletableFuture<Void> =
    sendEmail(to, generateDpmReceivedEmail(model), "DPM Received: ${model.dpmType}")

  @Async
  fun sendResetPasswordEmail(to: String, model: Reset): CompletableFuture<Void> =
    sendEmail(to, generateResetPasswordEmail(model), "Password has been reset")

  @Async
  fun sendWelcomeEmail(to: String, model: Welcome): CompletableFuture<Void> =
    sendEmail(to, generateWelcomeEmail(model), "Welcome to UTS DPM")

  private fun sendEmail(to: String, html: String, subject: String): CompletableFuture<Void> {
    val message =
      Message.builder()
        .from(EMAIL_FROM)
        .to(to)
        .subject(subject)
        .html(html)
        .build()

    mailgunClient.sendMessage(DOMAIN, message)
    return CompletableFuture.completedFuture(null)
  }

  private fun generateDpmReceivedEmail(model: DpmReceived): String =
    FreeMarkerTemplateUtils.processTemplateIntoString(
      fmConfiguration.getTemplate("dpm-received.ftlh"),
      model.toMap()
    )

  private fun generatePointsBalanceEmail(model: PointsBalance): String =
    FreeMarkerTemplateUtils.processTemplateIntoString(
      fmConfiguration.getTemplate("points-balance.ftlh"),
      model.toMap()
    )

  private fun generateWelcomeEmail(model: Welcome): String =
    FreeMarkerTemplateUtils.processTemplateIntoString(
      fmConfiguration.getTemplate("welcome.ftlh"),
      model.toMap()
    )

  private fun generateResetPasswordEmail(model: Reset): String =
    FreeMarkerTemplateUtils.processTemplateIntoString(
      fmConfiguration.getTemplate("reset-password.ftlh"),
      model.toMap()
    )
}
