package com.tunjicus.utsdpm.models.excel

interface ExcelHeader {
    fun getHeaders(): List<Pair<String, Int>>
}