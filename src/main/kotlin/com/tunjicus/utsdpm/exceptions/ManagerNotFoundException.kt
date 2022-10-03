package com.tunjicus.utsdpm.exceptions

class ManagerNotFoundException(name: String): RuntimeException("Failed to find manager with name: $name")
