package com.tunjicus.utsdpm.models.excel

import com.tunjicus.utsdpm.entities.User

class UserRow(user: User) : ExcelRow {
  private val lastName = user.lastname
  private val firstName = user.firstname
  private val points = user.points?.toString()
  private val manager = "${user.manager?.firstname} ${user.manager?.lastname}"

  override fun getRow(): List<String?> = listOf(lastName, firstName, points, manager)
}
