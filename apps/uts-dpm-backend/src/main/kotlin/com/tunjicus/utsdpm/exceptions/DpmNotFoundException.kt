package com.tunjicus.utsdpm.exceptions

class DpmNotFoundException(id: Int): RuntimeException("Failed with find a dpm with the id '$id'")
