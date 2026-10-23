package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.navigation.Screen
import com.example.ui.screens.academics.StaffMarksWorkspace
import com.example.ui.screens.operations.OperationsHubScreen
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
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class AdminWorkflowUiTest {
  @get:Rule val compose = createComposeRule()
  private fun repository(role: UserRole): ErpDataRepository {
    val repository = ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    repository.switchRole(role)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil { repository.currentUser.value.role == role && repository.students.value.isNotEmpty() }
    return repository
  }
  private fun capture(name: String) {
    compose.waitForIdle()
    File("build/reports/redesign/admin").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/redesign/admin/$name.png")
  }
  @Test fun staffCanSaveReviewAndPublishDraftWithoutRemovingReports() {
    val repository = repository(UserRole.TEACHER)
    var reportOpened = false
    compose.setContent { ScholaTheme(false) { Surface(Modifier.fillMaxSize()) { StaffMarksWorkspace(repository, { reportOpened = true }) } } }
    compose.onNodeWithTag("marks_term").performScrollTo().performTextInput("UI verification term")
    compose.onNodeWithTag("marks_subject").performScrollTo().performTextInput("Mathematics")
    compose.onNodeWithTag("marks_obtained").performScrollTo().performTextInput("72")
    compose.onNodeWithTag("save_draft_mark").performScrollTo().assertIsEnabled().performClick()
    compose.waitUntil { repository.reportCards.value.any { it.term == "UI verification term" } }
    assertFalse(repository.reportCards.value.first { it.term == "UI verification term" }.published)
    capture("marks_draft_light")
    compose.onNodeWithText("Review & publish term").performScrollTo().performClick()
    compose.onNodeWithText("Publish results?").assertExists()
    capture("marks_review_light")
    compose.onNodeWithText("Publish").performClick()
    compose.waitUntil { repository.reportCards.value.first { it.term == "UI verification term" }.published }
    compose.onNodeWithTag("save_draft_mark").performScrollTo().assertIsNotEnabled()
    compose.onNodeWithText("View report cards & student details").performScrollTo().performClick()
    assertTrue(reportOpened)
  }

  @Test @Config(qualifiers = "w320dp-h640dp-xhdpi")
  fun narrowLargeTextDirectorySearchRetainsWorkingDestination() {
    val repository = repository(UserRole.ADMIN)
    var destination = ""
    compose.setContent {
      val density = LocalDensity.current
      CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
        ScholaTheme(true) { Surface(Modifier.fillMaxSize()) { OperationsHubScreen(repository, { destination = it }, {}, {}, {}, {}) } }
      }
    }
    compose.onNodeWithTag("module_search").performTextInput("receipts")
    compose.onNodeWithTag("workspace_fees").performScrollTo().assertHasClickAction()
    capture("directory_narrow_dark")
    compose.onNodeWithTag("workspace_fees").performClick()
    assertEquals(Screen.Fees.route, destination)
  }

  @Test @Config(qualifiers = "w800dp-h1100dp-xhdpi")
  fun tabletDirectoryRendersInBothThemes() {
    val repository = repository(UserRole.ADMIN)
    var dark by mutableStateOf(false)
    compose.setContent { ScholaTheme(dark) { Surface(Modifier.fillMaxSize()) { OperationsHubScreen(repository, {}, {}, {}, {}, {}) } } }
    capture("directory_tablet_light")
    compose.runOnIdle { dark = true }
    capture("directory_tablet_dark")
  }
}
