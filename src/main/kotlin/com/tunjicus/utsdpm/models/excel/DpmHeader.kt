package com.tunjicus.utsdpm.models.excel

import com.tunjicus.utsdpm.helpers.ColumnWidth

class DpmHeader : ExcelHeader {
    private val firstName = "First Name"
    private val lastName = "Last Name"
    private val block = "Block"
    private val location = "Location"
    private val startTime = "Start Time"
    private val endTime = "End Time"
    private val date = "Date"
    private val type = "Type"
    private val points = "Points"
    private val notes = "Notes"
    private val status = "Status"
    private val created = "Created"
    private val createdBy = "Created By"

    override fun getHeaders(): List<Pair<String, Int>> = listOf(
        Pair(firstName, ColumnWidth.LARGE),
        Pair(lastName, ColumnWidth.LARGE),
        Pair(block, ColumnWidth.MEDIUM),
        Pair(location, ColumnWidth.MEDIUM),
        Pair(startTime, ColumnWidth.MEDIUM),
        Pair(endTime, ColumnWidth.MEDIUM),
        Pair(date, ColumnWidth.MEDIUM),
        Pair(type, ColumnWidth.EXTRA_LARGE),
        Pair(points, ColumnWidth.SMALL),
        Pair(notes, ColumnWidth.NOTES),
        Pair(status, ColumnWidth.STATUS),
        Pair(created, ColumnWidth.LARGE),
        Pair(createdBy, ColumnWidth.LARGE),
    )
}