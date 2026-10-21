package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme

/** Public preference only; never stores credentials or school data. */
enum class Appearance(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System");
  fun usesDark(systemDark: Boolean) = this == DARK || (this == SYSTEM && systemDark)
  companion object {
    fun parse(value: String?) = entries.firstOrNull { it.name == value } ?: LIGHT
  }
}
class AppearancePreference(context: Context) {
  private val preferences = context.applicationContext.getSharedPreferences("revenex_appearance", Context.MODE_PRIVATE)
  var selection by mutableStateOf(Appearance.parse(preferences.getString("mode", null)))
    private set
  fun select(value: Appearance) {
    selection = value
    preferences.edit().putString("mode", value.name).apply()
  }
}
val LocalAppearance = staticCompositionLocalOf<AppearancePreference?> { null }
@Composable
fun rememberAppearancePreference(): AppearancePreference {
  val context = LocalContext.current
  return remember(context.applicationContext) { AppearancePreference(context) }
}
