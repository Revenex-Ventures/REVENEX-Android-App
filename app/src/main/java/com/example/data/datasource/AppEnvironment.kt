package com.example.data.datasource

enum class DataMode(val label: String, val description: String) {
  DEMO("Demo Mode", "Standalone local simulation with full Indian school data"),
  SUPABASE_DEMO("Supabase Demo", "Authenticated school demo backed by Supabase")
}

data class ServiceIntegrationStatus(
  val razorpayReady: Boolean = false,
  val geminiAiReady: Boolean = false
)

object AppEnvironment {
  var currentMode: DataMode = DataMode.DEMO
    private set

  fun setMode(mode: DataMode) {
    currentMode = mode
  }

  fun getIntegrationStatus(): ServiceIntegrationStatus {
    return ServiceIntegrationStatus(
      razorpayReady = false, // Ready for key injection
      geminiAiReady = false  // Ready for API key injection
    )
  }
}
