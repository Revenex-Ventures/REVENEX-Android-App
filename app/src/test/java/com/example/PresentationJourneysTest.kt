package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.StudentDashboardScreen
import com.example.ui.theme.ScholaTheme
import com.example.ui.navigation.Screen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class PresentationJourneysTest {
  @get:Rule val compose=createComposeRule()
  @Test fun demoRemovesAdminAndStillOpensTheSelectedPrincipalPortal() {
    val repo=ErpDataRepository.getInstance()
    repo.setDataMode(DataMode.DEMO)
    var entered=false
    compose.setContent {ScholaTheme(false){AuthScreen(repo,{entered=true})}}
    compose.onNodeWithTag("role_select_admin").assertDoesNotExist()
    compose.onNodeWithTag("try_demo").performScrollTo().performClick()
    compose.onNodeWithTag("role_select_admin").assertDoesNotExist()
    compose.onNodeWithTag("role_select_principal").performScrollTo().performClick()
    compose.onNodeWithTag("submit_login_button").performScrollTo().assertIsDisplayed().performClick()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil {entered}
    assertEquals(UserRole.PRINCIPAL,repo.currentUser.value.role)
  }
  @Test fun familyFeeShortcutRetainsItsActualDestination() {
    val repo=ErpDataRepository.getInstance()
    repo.setDataMode(DataMode.DEMO);repo.switchRole(UserRole.PARENT)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    compose.waitUntil {repo.currentUser.value.role==UserRole.PARENT}
    var destination=""
    compose.setContent {ScholaTheme(false){StudentDashboardScreen(repo,{destination=it})}}
    compose.onNodeWithTag("student_dashboard_list").performScrollToNode(hasTestTag("family_fee_accounts"))
    compose.onNodeWithTag("family_fee_accounts").performClick()
    assertEquals(Screen.Fees.route,destination)
  }
}
