package com.tunjicus.utsdpm.exceptions

class UserNotFoundException : RuntimeException {
  constructor(name: String) : super("Failed to find user with the name $name")
  constructor(id: Int) : super ("Failed to find user with the id: $id")
}
