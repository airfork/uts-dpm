package com.tunjicus.utsdpm.helpers

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("singleton")
class Constants {
    @Value("\${app.base-url}")
    private lateinit var _baseUrl: String

    @Value("\${app.mailgun.key}")
    private lateinit var _mailgunKey: String

    fun baseUrl() = _baseUrl
    fun mailgunKey() = _mailgunKey
}