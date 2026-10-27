package com.example.data.model

/** Android availability only. This does not change server roles or authorization. */
object MobileRoleAccess {
  val supportedRoles = setOf(UserRole.PRINCIPAL, UserRole.TEACHER, UserRole.STUDENT, UserRole.PARENT)
  const val unsupportedMessage = "Administrator accounts are not supported in the Android app. Administrator access is reserved for the future Admin web portal. Sign in with a supported school account or contact your school."
  fun supports(role: UserRole): Boolean = role in supportedRoles
  fun requireSupported(role: UserRole) {
    if (!supports(role)) throw UnsupportedMobileRoleException()
  }
}

class UnsupportedMobileRoleException : IllegalStateException(MobileRoleAccess.unsupportedMessage)
