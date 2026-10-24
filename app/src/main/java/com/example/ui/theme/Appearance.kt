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
  private val appContext = context.applicationContext
  private val preferences = context.applicationContext.getSharedPreferences("revenex_appearance", Context.MODE_PRIVATE)
  var selection by mutableStateOf(Appearance.parse(preferences.getString("mode", null)))
    private set
  fun select(value: Appearance) {
    selection = value
    preferences.edit().putString("mode", value.name).apply()
    AppearanceSystemBars.synchronize(appContext)
  }
}

/** Persist only app presentation. Android's application setter maps AUTO to
 * UI_MODE_NIGHT_UNDEFINED (clears the app override), unlike global setNightMode.
 * Verified against AOSP Android 12L/14 UiModeManagerService and on-device below.
 */
object AppearanceSystemBars {
  private var lastNight: Int? = null
  fun synchronize(context: Context) {
    if (android.os.Build.VERSION.SDK_INT < 31) return
    val prefs=context.getSharedPreferences("revenex_appearance",Context.MODE_PRIVATE)
    val choice=Appearance.parse(prefs.getString("mode",null))
    val night=when(choice) {
      Appearance.LIGHT -> android.app.UiModeManager.MODE_NIGHT_NO
      Appearance.DARK -> android.app.UiModeManager.MODE_NIGHT_YES
      Appearance.SYSTEM -> android.app.UiModeManager.MODE_NIGHT_AUTO
    }
    if(lastNight!=night) {
      lastNight=night
      context.getSystemService(android.app.UiModeManager::class.java)?.setApplicationNightMode(night)
    }
  }
  fun install(context: Context) {
    synchronize(context)
  }
}
val LocalAppearance = staticCompositionLocalOf<AppearancePreference?> { null }
@Composable
fun rememberAppearancePreference(): AppearancePreference {
  val context = LocalContext.current
  return remember(context.applicationContext) { AppearancePreference(context) }
}
