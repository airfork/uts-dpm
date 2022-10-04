package com.tunjicus.utsdpm.emailModels

data class Email(
    val from: String,
    val to: String,
    val subject: String,
    val html: String
)
