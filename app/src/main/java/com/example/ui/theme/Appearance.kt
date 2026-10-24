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

/** Persist only the platform's application night presentation, never global settings.
 * AUTO is time/location based and must not be used to mean "follow system".
 */
object AppearanceSystemBars {
  private var lastNight: Int? = null
  fun synchronize(context: Context) {
    if (android.os.Build.VERSION.SDK_INT < 31) return
    val prefs=context.getSharedPreferences("revenex_appearance",Context.MODE_PRIVATE)
    val choice=Appearance.parse(prefs.getString("mode",null))
    val globalDark=android.content.res.Resources.getSystem().configuration.uiMode and
      android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val night=if(choice.usesDark(globalDark)) android.app.UiModeManager.MODE_NIGHT_YES else android.app.UiModeManager.MODE_NIGHT_NO
    if(lastNight!=night) {
      lastNight=night
      context.getSystemService(android.app.UiModeManager::class.java)?.setApplicationNightMode(night)
    }
  }
  fun install(context: Context) {
    synchronize(context)
    if(android.os.Build.VERSION.SDK_INT >= 31) {
      androidx.core.content.ContextCompat.registerReceiver(context,object: android.content.BroadcastReceiver() {
        override fun onReceive(context: Context,intent: android.content.Intent) { synchronize(context) }
      },android.content.IntentFilter(android.content.Intent.ACTION_CONFIGURATION_CHANGED),androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
    }
  }
}
val LocalAppearance = staticCompositionLocalOf<AppearancePreference?> { null }
@Composable
fun rememberAppearancePreference(): AppearancePreference {
  val context = LocalContext.current
  return remember(context.applicationContext) { AppearancePreference(context) }
}
