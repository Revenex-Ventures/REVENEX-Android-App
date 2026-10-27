package com.example

import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.*
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.communication.NotificationsScreen
import com.example.ui.theme.ScholaTheme
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
@Config(sdk=[34], qualifiers="w393dp-h852dp-xhdpi")
class RevenexRedesignScreenshotTest {
  @get:Rule val compose = createComposeRule()
  private fun capture(name: String) {
    File("build/reports/redesign").mkdirs()
    compose.onRoot().captureRoboImage("build/reports/redesign/$name.png")
  }
  private fun portal(role: UserRole, dark: Boolean = false, fontScale: Float = 1f) {
    val repository = ErpDataRepository.getInstance()
    repository.setDataMode(DataMode.DEMO)
    repository.switchRole(role)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil { repository.currentUser.value.role == role }
    compose.setContent {
      val user by repository.currentUser.collectAsState()
      val density = androidx.compose.ui.platform.LocalDensity.current
      CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, fontScale)) {
      ScholaTheme(darkTheme=dark) {
        ScholaResponsiveScaffold(Screen.Dashboard.route,user,2,{},{},{},{},{}) {
          when(role) {
            UserRole.PRINCIPAL -> PrincipalDashboardScreen(repository,{},{},{},{})
            UserRole.ADMIN -> com.example.ui.screens.auth.UnsupportedMobileAccountScreen {}
            UserRole.TEACHER -> TeacherDashboardScreen(repository,{},{},{})
            UserRole.STUDENT -> StudentDashboardScreen(repository,{})
            // Production NavHost routes parents to this shared family dashboard.
            UserRole.PARENT -> StudentDashboardScreen(repository,{})
          }
        }
      }
      }
    }
    val width = org.robolectric.RuntimeEnvironment.getApplication().resources.configuration.screenWidthDp
    val suffix = if (width == 393 && fontScale == 1f) "" else "_${width}dp_${fontScale}x"
    capture("${role.name}_${if(dark) "dark" else "light"}$suffix")
  }
  @Test fun principalLight() = portal(UserRole.PRINCIPAL)
  @Test fun adminLight() = portal(UserRole.ADMIN)
  @Test fun teacherLight() = portal(UserRole.TEACHER)
  @Test fun studentLight() = portal(UserRole.STUDENT)
  @Test fun parentLight() = portal(UserRole.PARENT)
  @Test fun principalDark() = portal(UserRole.PRINCIPAL,true)
  @Test fun adminDark() = portal(UserRole.ADMIN,true)
  @Test fun studentDark() = portal(UserRole.STUDENT,true)
  @Test fun parentDark() = portal(UserRole.PARENT,true)
  @Test fun teacherDark() = portal(UserRole.TEACHER,true)
  @Test @Config(qualifiers="w800dp-h1100dp-xhdpi") fun tabletAdmin() = portal(UserRole.ADMIN)
  @Test @Config(qualifiers="w320dp-h640dp-xhdpi") fun narrowStudentLargeText() = portal(UserRole.STUDENT, fontScale=1.5f)
  @Test fun auth() {
    compose.setContent { ScholaTheme(false) { AuthScreen(ErpDataRepository.getInstance(),{}) } }
    capture("auth")
  }
  @Test fun settingsDark() {
    val preference = com.example.ui.theme.AppearancePreference(org.robolectric.RuntimeEnvironment.getApplication())
    preference.select(com.example.ui.theme.Appearance.DARK)
    compose.setContent { CompositionLocalProvider(com.example.ui.theme.LocalAppearance provides preference) {
      ScholaTheme { ProfileScreen(ErpDataRepository.getInstance(),{},{}) }
    } }
    capture("settings_dark")
  }
  @Test fun authDark() {
    compose.setContent { ScholaTheme(true) { AuthScreen(ErpDataRepository.getInstance(),{}) } }
    capture("auth_dark")
  }
  @Test fun settingsLight() {
    val preference = com.example.ui.theme.AppearancePreference(org.robolectric.RuntimeEnvironment.getApplication())
    preference.select(com.example.ui.theme.Appearance.LIGHT)
    compose.setContent { CompositionLocalProvider(com.example.ui.theme.LocalAppearance provides preference) {
      ScholaTheme { ProfileScreen(ErpDataRepository.getInstance(),{},{}) }
    } }
    capture("settings_light")
  }
  @Test fun inboxDark() {
    compose.setContent { ScholaTheme(true) { NotificationsScreen(ErpDataRepository.getInstance(),{}) } }
    capture("inbox_dark")
  }
  @Test fun admissionForm() {
    compose.setContent { ScholaTheme(false) { Surface(Modifier.fillMaxSize()) {
      AddStudentDialog(onDismiss={},onAddStudent={})
    } } }
    capture("admission_form")
  }
  @Test fun admissionFormDark() {
    compose.setContent { ScholaTheme(true) { Surface(Modifier.fillMaxSize()) {
      AddStudentDialog(onDismiss={},onAddStudent={})
    } } }
    capture("admission_form_dark")
  }
}
