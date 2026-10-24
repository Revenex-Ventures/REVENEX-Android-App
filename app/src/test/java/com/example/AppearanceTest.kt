package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.Appearance
import com.example.ui.theme.AppearancePreference
import com.example.ui.navigation.NavConfig
import com.example.data.model.UserRole
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class AppearanceTest {
  @Test fun freshInstallDefaultsLightAndSelectionSurvivesNewStore() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    context.getSharedPreferences("revenex_appearance", 0).edit().clear().commit()
    assertEquals(Appearance.LIGHT, AppearancePreference(context).selection)
    Appearance.entries.forEach {
      AppearancePreference(context).select(it)
      assertEquals(it, AppearancePreference(context).selection)
    }
  }
  @Test fun modesAreExclusiveAndSystemFollowsConfiguration() {
    assertFalse(Appearance.LIGHT.usesDark(true))
    assertFalse(Appearance.LIGHT.usesDark(false))
    assertTrue(Appearance.DARK.usesDark(false))
    assertTrue(Appearance.DARK.usesDark(true))
    assertFalse(Appearance.SYSTEM.usesDark(false))
    assertTrue(Appearance.SYSTEM.usesDark(true))
    assertEquals(Appearance.LIGHT, Appearance.parse("invalid"))
  }
  @Test fun systemClearsPreviouslyForcedApplicationModeWithoutChangingAndroidSetting() {
    val context=ApplicationProvider.getApplicationContext<android.content.Context>()
    val manager=context.getSystemService(android.app.UiModeManager::class.java)
    val androidSetting=manager.nightMode
    val preference=AppearancePreference(context)
    preference.select(Appearance.DARK)
    assertEquals(android.app.UiModeManager.MODE_NIGHT_YES,org.robolectric.Shadows.shadowOf(manager).applicationNightMode)
    preference.select(Appearance.SYSTEM)
    assertEquals(android.app.UiModeManager.MODE_NIGHT_AUTO,org.robolectric.Shadows.shadowOf(manager).applicationNightMode)
    assertEquals(androidSetting,manager.nightMode)
  }
  @Test fun everyPortalHasAtMostFiveDistinctPrimaryDestinations() {
    UserRole.entries.forEach { role ->
      val items = NavConfig.getBottomNavItems(role)
      assertTrue(items.size <= 5)
      assertEquals(items.size, items.map { it.route }.distinct().size)
    }
  }
}
