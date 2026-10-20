package com.example.data.datasource

import com.example.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import java.net.URI
import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer

data class SupabaseConfiguration(val url: String, val publishableKey: String) {
  fun validate() {
    val parsed = runCatching { URI(url) }.getOrNull()
    require(parsed?.scheme == "https" && !parsed.host.isNullOrBlank() && parsed.userInfo == null &&
      parsed.rawQuery == null && parsed.rawFragment == null) {
      "Set a valid HTTPS SUPABASE_URL in supabase.local.properties and rebuild."
    }
    require(publishableKey.startsWith("sb_publishable_") && publishableKey.length > 20 &&
      publishableKey.none { it.isWhitespace() }) {
      "Set SUPABASE_PUBLISHABLE_KEY in supabase.local.properties. Private and legacy keys are not accepted."
    }
  }
}

object SupabaseProvider {
  val json=Json { ignoreUnknownKeys=true; encodeDefaults=true }
  val configuration get() = SupabaseConfiguration(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)
  val client by lazy {
    configuration.validate()
    createSupabaseClient(configuration.url, configuration.publishableKey) {
      defaultSerializer=KotlinXSerializer(SupabaseProvider.json)
      install(Auth) {
        // Demo sessions are memory-only: no refresh tokens in backups/preferences.
        alwaysAutoRefresh = true
        autoLoadFromStorage = false
        autoSaveToStorage = false
      }
      install(Postgrest)
      install(Storage)
      install(Realtime)
      install(Functions)
    }
  }
}
