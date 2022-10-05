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
import java.util.concurrent.CompletableFuture
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

@Service
class EmailService(
  private val fmConfiguration: Configuration,
  private val environment: Environment,
  constants: Constants
) {

  private val mailgunClient: MailgunMessagesApi =
    MailgunClient.config(constants.mailgunKey()).createApi(MailgunMessagesApi::class.java)

  // send only to this email if in local profile
  @Value("\${app.email.override}") private lateinit var recipientOverride: String
  @Value("\${app.email.domain}") private lateinit var domain: String
  @Value("\${app.email.from}") private lateinit var emailFrom: String

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
    // TODO: Remove prod profile from if statement
    val recipient =
      if (environment.activeProfiles.contains("local") || environment.activeProfiles.contains("prod")) {
        recipientOverride
      } else {
        to
      }

    val message =
      Message.builder().from(emailFrom).to(recipient).subject(subject).html(html).build()
    mailgunClient.sendMessage(domain, message)
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
