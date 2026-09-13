package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.GlobalCommandPaletteModal
import com.example.ui.components.ScholaResponsiveScaffold
import com.example.ui.navigation.Screen
import com.example.ui.screens.academics.TimetableScreen
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.communication.NoticesScreen
import com.example.ui.screens.dashboard.PrincipalDashboardScreen
import com.example.ui.screens.fees.FeeManagementScreen
import com.example.ui.screens.students.StudentDetailScreen
import com.example.ui.screens.students.StudentListScreen
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScholaOSPreviewScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val artifactDir = "C:/Users/Prasanna/.gemini/antigravity-ide/brain/4f05eed0-c80d-4122-99f1-ba7d0cf4bc42/screenshots"

  private fun saveScreenshot(name: String) {
    val dir = File(artifactDir)
    if (!dir.exists()) dir.mkdirs()
    val path = "$artifactDir/$name.png"
    composeTestRule.onRoot().captureRoboImage(filePath = path)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/$name.png")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_01_principal_dashboard_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Dashboard.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          PrincipalDashboardScreen(
            repository = repository,
            onNavigateTo = {},
            onShowAddStudent = {},
            onShowAddTeacher = {},
            onShowCreateNotice = {}
          )
        }
      }
    }
    saveScreenshot("01_principal_dashboard_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_02_scholars_directory_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Students.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          StudentListScreen(
            repository = repository,
            onNavigateToStudentDetail = {},
            onShowAddStudent = {}
          )
        }
      }
    }
    saveScreenshot("02_scholars_directory_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_03_student_360_detail_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.StudentDetail.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          StudentDetailScreen(
            studentId = "REV-2026-001",
            repository = repository,
            onBack = {},
            onPayFee = { _, _ -> }
          )
        }
      }
    }
    saveScreenshot("03_student_360_detail_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_04_attendance_matrix_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Attendance.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          AttendanceScreen(
            repository = repository,
            onBack = {}
          )
        }
      }
    }
    saveScreenshot("04_attendance_matrix_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_05_fee_management_ledger_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Fees.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          FeeManagementScreen(
            repository = repository,
            onShowPaymentModal = { _, _ -> }
          )
        }
      }
    }
    saveScreenshot("05_fee_management_ledger_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_06_academics_timetable_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Timetable.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          TimetableScreen(repository = repository)
        }
      }
    }
    saveScreenshot("06_academics_timetable_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_07_audit_trail_notices_mobile() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Notices.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          NoticesScreen(
            repository = repository,
            onShowCreateNotice = {}
          )
        }
      }
    }
    saveScreenshot("07_audit_trail_notices_mobile")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
  fun preview_08_command_palette_modal() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      ScholaTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          PrincipalDashboardScreen(
            repository = repository,
            onNavigateTo = {},
            onShowAddStudent = {},
            onShowAddTeacher = {},
            onShowCreateNotice = {}
          )
          GlobalCommandPaletteModal(
            repository = repository,
            onDismiss = {},
            onNavigateTo = {},
            onTriggerAction = {}
          )
        }
      }
    }
    saveScreenshot("08_command_palette_modal")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.PixelTablet, sdk = [36])
  fun preview_09_tablet_executive_dashboard() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Dashboard.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          PrincipalDashboardScreen(
            repository = repository,
            onNavigateTo = {},
            onShowAddStudent = {},
            onShowAddTeacher = {},
            onShowCreateNotice = {}
          )
        }
      }
    }
    saveScreenshot("09_tablet_executive_dashboard")
  }

  @Test
  @Config(qualifiers = RobolectricDeviceQualifiers.PixelTablet, sdk = [36])
  fun preview_10_tablet_scholars_split_pane() {
    val repository = ErpDataRepository.getInstance()
    composeTestRule.setContent {
      val currentUser by repository.currentUser.collectAsState()
      ScholaTheme {
        ScholaResponsiveScaffold(
          currentRoute = Screen.Students.route,
          currentUser = currentUser,
          unreadNotificationsCount = 4,
          onNavigateToRoute = {},
          onOpenCommandPalette = {},
          onOpenNotifications = {},
          onOpenProfile = {},
          onLogout = {}
        ) {
          StudentListScreen(
            repository = repository,
            onNavigateToStudentDetail = {},
            onShowAddStudent = {}
          )
        }
      }
    }
    saveScreenshot("10_tablet_scholars_split_pane")
  }
}
