package com.example.data.datasource

import io.github.jan.supabase.auth.auth

enum class DataMode(val label: String, val description: String) {
  DEMO("Demo Mode", "Standalone local simulation with full Indian school data"),
  SUPABASE_DEMO("Supabase Demo", "Authenticated school demo backed by Supabase")
}

enum class IntegrationReadiness { LOCAL_ONLY, PUBLIC_CONFIGURATION_MISSING, SIGN_IN_REQUIRED, NOT_VERIFIED }

data class ServiceIntegrationStatus(
  val payments: IntegrationReadiness,
  val assistant: IntegrationReadiness,
  val assistantProvider: String = "OpenAI"
)

internal fun integrationReadiness(mode: DataMode, configured: Boolean, signedIn: Boolean): ServiceIntegrationStatus {
  val state = when {
    mode == DataMode.DEMO -> IntegrationReadiness.LOCAL_ONLY
    !configured -> IntegrationReadiness.PUBLIC_CONFIGURATION_MISSING
    !signedIn -> IntegrationReadiness.SIGN_IN_REQUIRED
    else -> IntegrationReadiness.NOT_VERIFIED
  }
  return ServiceIntegrationStatus(state, state)
}

object AppEnvironment {
  var currentMode: DataMode = DataMode.DEMO
    private set

  fun setMode(mode: DataMode) {
    currentMode = mode
  }

  fun getIntegrationStatus(): ServiceIntegrationStatus {
    val configured=runCatching { SupabaseProvider.configuration.validate() }.isSuccess
    val signedIn=currentMode==DataMode.SUPABASE_DEMO && configured &&
      runCatching { SupabaseProvider.client.auth.currentSessionOrNull()!=null }.getOrDefault(false)
    return integrationReadiness(currentMode,configured,signedIn)
  }
}
