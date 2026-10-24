package com.example.data.datasource

/** A response belongs to the initiating account even when its request cannot be unsent. */
internal suspend fun <T> confirmInSession(
  revision: () -> Long,
  active: () -> Boolean,
  persist: suspend () -> T,
  reload: suspend () -> Unit
): T {
  val started=revision()
  fun verify() { check(active() && revision()==started) {
    "Account changed. Check the original account before retrying."
  } }
  verify()
  val result=persist()
  verify() // Never reload into a different account or report its action as successful.
  reload()
  verify()
  return result
}
