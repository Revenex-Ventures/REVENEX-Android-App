package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.RevenexLaunchHost
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class LaunchEntryTest {
  @get:Rule val compose=createComposeRule()
  private fun repo()=ErpDataRepository.getInstance().also { it.setDataMode(DataMode.DEMO) }
  private fun capture(name:String) {
    compose.waitForIdle();File("build/reports/launch-entry").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/launch-entry/$name.png")
  }
  @Test fun everyPortalIsOnlyAHintAndBackReturnsToEntry() {
    val repo=repo();val original=repo.currentUser.value
    var entered=false
    compose.setContent {ScholaTheme(false){AuthScreen(repo,{entered=true})}}
    for (portal in listOf("principal","admin","teacher","family")) {
      compose.onNodeWithTag("portal_$portal").performScrollTo().performClick()
      compose.onNodeWithTag("login_email").assertExists()
      compose.onNodeWithTag("submit_supabase_login").assertIsNotEnabled()
      assertEquals(original,repo.currentUser.value);assertFalse(entered)
      compose.onNodeWithTag("entry_back").assertIsDisplayed().performClick()
    }
    compose.onNodeWithText("Welcome to Revenex").assertExists()
  }
  @Test fun photoEntryLoginAndSeparateDemoRenderInBothPalettes() {
    val repo=repo();var dark by mutableStateOf(false)
    compose.setContent {ScholaTheme(dark){AuthScreen(repo,{})}}
    capture("account_entry_light")
    compose.runOnIdle {dark=true};capture("account_entry_dark")
    compose.onNodeWithTag("portal_teacher").performScrollTo().performClick()
    capture("teacher_login_dark")
    compose.runOnIdle {dark=false};capture("teacher_login_light")
    compose.onNodeWithTag("try_demo").performScrollTo().performClick()
    capture("demo_picker_light")
    compose.runOnIdle {dark=true};capture("demo_picker_dark");compose.runOnIdle {dark=false}
    compose.onNodeWithTag("role_select_parent").performScrollTo().assertExists()
    capture("local_demo_light")
  }
  @Test fun schoolSignInIsExplicitlySupabaseAndSeparateFromDemo() {
    val repo=repo();var entered=false
    compose.setContent {ScholaTheme(false){AuthScreen(repo,{entered=true})}}
    compose.onNodeWithTag("portal_teacher").performScrollTo().performClick()
    compose.onNodeWithTag("supabase_entry_label").performScrollTo().assertTextEquals("School sign-in Â· Supabase")
    compose.onNodeWithTag("submit_supabase_login").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
    compose.onNodeWithTag("role_select_teacher").assertDoesNotExist()
    assertFalse(entered)
  }
  @Test fun guardianDemoRetainsParentIdentity() {
    val repo=repo();var entered=false
    compose.setContent {ScholaTheme(false){AuthScreen(repo,{entered=true})}}
    compose.onNodeWithTag("try_demo").performScrollTo().performClick()
    compose.onNodeWithTag("role_select_parent").performScrollTo().performClick()
    compose.onNodeWithTag("submit_login_button").performScrollTo().assertIsDisplayed().performClick()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil {entered}
    assertEquals(UserRole.PARENT,repo.currentUser.value.role)
  }
  @Test fun actualRoutedAppOpensTeacherDashboardAfterLocalDemoSelection() {
    val repository=repo()
    compose.setContent {ScholaTheme(false){ScholaErpApp(false,false,"",{_,_->},{})}}
    compose.onNodeWithTag("try_demo").performScrollTo().performClick()
    compose.onNodeWithTag("role_select_teacher").performScrollTo().performClick()
    compose.onNodeWithTag("submit_login_button").performScrollTo().assertIsDisplayed().performClick()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil(10000) {compose.onAllNodesWithTag("teacher_dashboard_list").fetchSemanticsNodes().isNotEmpty()}
    capture("routed_teacher_dashboard_light")
    compose.onNodeWithTag("portal_teacher").assertDoesNotExist()
    val actor=repository.currentUser.value
    val cohort=repository.teachers.value.firstOrNull {it.email.equals(actor.email,true)||it.name.equals(actor.name,true)}?.assignedClasses?.firstOrNull() ?: actor.associatedClass
    assertTrue(cohort.isNotBlank())
    compose.onNodeWithText("Mark attendance").performScrollTo().performClick()
    compose.waitUntil(10000) {compose.onAllNodesWithTag("attendance_matrix_view").fetchSemanticsNodes().isNotEmpty()}
    compose.onNodeWithText("Class $cohort").assertExists()
    capture("routed_teacher_class_attendance")
  }
  @Test fun introInitializesContentConcurrentlyAndDoesNotReplayOnRecomposition() {
    compose.mainClock.autoAdvance=false
    var ready by mutableStateOf(false);var recomposition by mutableIntStateOf(0)
    var initialized=false
    compose.setContent {ScholaTheme(false){
      RevenexLaunchHost(true,ready,false) {
        SideEffect {initialized=true}
        Text("Application $recomposition",Modifier.testTag("app_content"))
      }
    }}
    compose.runOnIdle {assertTrue(initialized);ready=true}
    // Commit native-exit state and start its effect before measuring animation time.
    compose.mainClock.advanceTimeByFrame()
    compose.waitForIdle()
    compose.mainClock.advanceTimeBy(800)
    capture("brand_reveal_mid")
    compose.mainClock.advanceTimeBy(500)
    capture("brand_outgoing_opaque")
    compose.mainClock.advanceTimeBy(400)
    compose.waitForIdle()
    compose.onNodeWithTag("revenex_brand_intro").assertDoesNotExist()
    compose.runOnIdle {recomposition++}
    compose.mainClock.advanceTimeByFrame()
    compose.onNodeWithTag("app_content").assertExists()
    compose.onNodeWithTag("revenex_brand_intro").assertDoesNotExist()
  }
  @Test fun reducedMotionDoesNotWaitForRevealAnimation() {
    compose.setContent {ScholaTheme(false){RevenexLaunchHost(true,true,true){Text("Ready")}}}
    compose.onNodeWithTag("revenex_brand_intro").assertDoesNotExist()
    compose.onNodeWithText("Ready").assertExists()
  }
  @Test @Config(qualifiers="w320dp-h640dp-xhdpi")
  fun narrowLargeTextKeepsAllPortalsAndPasswordControlsReachable() {
    val repo=repo()
    compose.setContent {
      val d=LocalDensity.current
      CompositionLocalProvider(LocalDensity provides Density(d.density,1.5f)) {ScholaTheme(false){AuthScreen(repo,{})}}
    }
    compose.onNodeWithTag("portal_family").performScrollTo().performClick()
    compose.onNodeWithTag("login_password").performScrollTo().performTextInput("synthetic-input")
    compose.onNodeWithContentDescription("Show password").performClick()
    compose.onNodeWithContentDescription("Hide password").assertExists()
    compose.onNodeWithTag("submit_supabase_login").performScrollTo().assertIsDisplayed().assertIsNotEnabled()
    capture("login_narrow_large_text")
  }
  @Test @Config(qualifiers="w840dp-h900dp-xhdpi")
  fun tabletPhotoAndEntryRemainUsable() {
    val repo=repo()
    compose.setContent {ScholaTheme(false){AuthScreen(repo,{})}}
    compose.onNodeWithTag("portal_family").performScrollTo().assertIsDisplayed()
    capture("account_entry_tablet_light")
  }
}
