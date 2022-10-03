package com.tunjicus.utsdpm.exceptions

class NameNotFoundException(name: String) : RuntimeException("Failed to find user with the name '$name'")
