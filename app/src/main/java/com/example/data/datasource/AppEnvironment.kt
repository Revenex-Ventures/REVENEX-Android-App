package com.example.data.datasource

enum class DataMode(val label: String, val description: String) {
  DEMO("Demo Mode", "Standalone local simulation with full Indian school data"),
  PRODUCTION("Production Mode", "Live Firebase Cloud synchronization")
}

data class ServiceIntegrationStatus(
  val firebaseAuthReady: Boolean = false,
  val firestoreReady: Boolean = false,
  val fcmReady: Boolean = false,
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
      firebaseAuthReady = false,
      firestoreReady = false,
      fcmReady = false,
      razorpayReady = false, // Ready for key injection
      geminiAiReady = false  // Ready for API key injection
    )
  }
}
