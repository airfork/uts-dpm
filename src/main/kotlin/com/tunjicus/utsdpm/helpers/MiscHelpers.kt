package com.tunjicus.utsdpm.helpers

object MiscHelpers {
  fun generateDpmStatusMessage(approved: Boolean, ignored: Boolean): String {
    if (approved && !ignored) return "Approved"
    if (approved) return "Approved; invisible to driver"
    if (!ignored) return "Not looked at"
    return "Denied"
  }
}
