package com.example.data.datasource

/** Fixed, safe messages only: never display SDK response bodies, tokens or passwords. */
internal fun supabaseSignInFailure(stage: String, error: Throwable): String {
  val causes=generateSequence(error) { it.cause }.take(8).toList()
  if(causes.any { it is java.io.IOException || it::class.simpleName?.contains("Timeout")==true })
    return "Cannot reach Supabase. Check internet access and try again."
  val diagnostic=causes.joinToString(" ") { it.message.orEmpty() }.lowercase()
  if(stage=="auth") return when {
    "email_not_confirmed" in diagnostic || "email not confirmed" in diagnostic -> "This account email is not confirmed. Ask the school operator to confirm the demo Auth user."
    "invalid_credentials" in diagnostic || "invalid login credentials" in diagnostic -> "Email or password was rejected by Supabase Auth. Use the exact account created in Authentication > Users; app demo personas do not create cloud accounts."
    else -> "Supabase Auth sign-in failed. Check the assigned Auth account and password, email confirmation and project settings."
  }
  if(stage=="profile") return "Sign-in succeeded, but the school profile is missing or inaccessible. The operator must link this Auth user in profiles with a school, session and role (run the demo seed for the four demo accounts)."
  if(stage=="role") return "Sign-in succeeded, but this account has no supported Android role. Ask the school operator to check its profile."
  return "Sign-in succeeded, but school data could not load ($stage). Ask the operator to check the numbered SQL migrations and access policies. No local sample data was substituted."
}
