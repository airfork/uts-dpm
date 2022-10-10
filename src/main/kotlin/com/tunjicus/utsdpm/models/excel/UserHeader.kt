package com.tunjicus.utsdpm.models.excel

import com.tunjicus.utsdpm.helpers.ColumnWidth

class UserHeader : ExcelHeader {
    private val lastName = "Last Name"
    private val firstName = "First Name"
    private val points = "Points"
    private val manager = "Manager"

    override fun getHeaders(): List<Pair<String, Int>> = listOf(
        Pair(lastName, ColumnWidth.LARGE),
        Pair(firstName, ColumnWidth.LARGE),
        Pair(points, ColumnWidth.SMALL),
        Pair(manager, ColumnWidth.LARGE),
    )
}