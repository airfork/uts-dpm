package com.tunjicus.utsdpm.exceptions

class UserNameNotFoundException(name: String) : RuntimeException("Failed to find user with the name '$name'")
