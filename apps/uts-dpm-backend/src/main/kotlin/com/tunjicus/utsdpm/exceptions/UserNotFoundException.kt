package com.tunjicus.utsdpm.exceptions

class UserNotFoundException(id: Int) : RuntimeException("Failed to find user with id: $id")
