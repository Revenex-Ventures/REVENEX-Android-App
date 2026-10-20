package com.example
import com.example.data.datasource.supabaseSignInFailure
import org.junit.Assert.*
import org.junit.Test
class SupabaseSignInDiagnosticsTest {
 @Test fun authAndMissingProfileAreDistinct() {
  assertTrue(supabaseSignInFailure("auth",Exception("invalid_credentials")).contains("rejected"))
  assertTrue(supabaseSignInFailure("profile",Exception()).contains("Sign-in succeeded"))
  assertTrue(supabaseSignInFailure("inventory",Exception()).contains("inventory"))
 }
 @Test fun privateDiagnosticBodiesNeverReachTheScreen() {
  assertFalse(supabaseSignInFailure("auth",Exception("secret-password-private-token")).contains("secret-password"))
  assertTrue(supabaseSignInFailure("profile",java.io.IOException()).contains("internet"))
 }
}
