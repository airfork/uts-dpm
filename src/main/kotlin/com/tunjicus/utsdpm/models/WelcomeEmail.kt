package com.tunjicus.utsdpm.models

import com.tunjicus.utsdpm.helpers.formatCurrentYear

open class WelcomeEmail(
    val name: String,
    val password: String,
    val url: String,
) {
    private val year: String = formatCurrentYear()
    fun toMap(): Map<String, String> =
        mapOf(
            Pair("name", name),
            Pair("password", password),
            Pair("url", url),
            Pair("year", year)
        )
}
