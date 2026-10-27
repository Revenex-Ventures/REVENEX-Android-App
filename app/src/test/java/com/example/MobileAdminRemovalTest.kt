package com.example

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.*
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.ScholaResponsiveScaffold
import com.example.ui.navigation.*
import com.example.ui.screens.operations.WorkspaceDirectory
import com.example.ui.theme.ScholaTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w393dp-h820dp-xhdpi")
class MobileAdminRemovalTest {
 @get:Rule val compose=createComposeRule()
 private fun admin():ErpDataRepository {
  val repo=ErpDataRepository.getInstance();repo.setDataMode(DataMode.DEMO);repo.switchRole(UserRole.ADMIN)
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  compose.waitUntil{repo.currentUser.value.role==UserRole.ADMIN}
  return repo
 }
 @Test fun backendAdminRoleIsRetainedButMobileAuthRejectsWithoutRoleFallback() {
  assertEquals(UserRole.ADMIN,UserRole.valueOf("ADMIN"))
  val failure=runCatching{MobileRoleAccess.requireSupported(UserRole.ADMIN)}.exceptionOrNull()
  assertTrue(failure is UnsupportedMobileRoleException)
  assertEquals(MobileRoleAccess.unsupportedMessage,supabaseSignInFailure("role",requireNotNull(failure)))
  for(role in MobileRoleAccess.supportedRoles){MobileRoleAccess.requireSupported(role);assertTrue(NavConfig.getBottomNavItems(role).isNotEmpty())}
  assertTrue(NavConfig.getBottomNavItems(UserRole.ADMIN).isEmpty())
  for(destination in WorkspaceDirectory.destinations){assertFalse(UserRole.ADMIN in destination.roles);assertNull(NavConfig.selectedPrimaryRoute(UserRole.ADMIN,destination.route))}
  assertTrue(WorkspaceDirectory.visible(UserRole.ADMIN).isEmpty())
 }
 @Test fun restoredAdminIdentityCannotRenderAnyWorkspaceAndCanReturnToSignIn() {
  val repo=admin();var dark by mutableStateOf(false)
  compose.setContent{ScholaTheme(dark){ScholaErpApp(false,false,"",{_,_->},{})}}
  File("build/reports/admin-removal").mkdirs()
  for(palette in listOf(false,true)){
   compose.runOnIdle{dark=palette};compose.waitForIdle()
   compose.onNodeWithTag("unsupported_mobile_account").assertIsDisplayed()
   compose.onNodeWithText(MobileRoleAccess.unsupportedMessage).assertExists()
   compose.onNodeWithTag("admin_dashboard_list").assertDoesNotExist()
   compose.onNodeWithTag("principal_dashboard_list").assertDoesNotExist()
   compose.onNodeWithContentDescription("Home").assertDoesNotExist()
   compose.onRoot().captureRoboImage("build/reports/admin-removal/unsupported_${if(palette)"dark"else"light"}.png")
  }
  compose.onNodeWithTag("unsupported_mobile_sign_out").performClick()
  org.robolectric.shadows.ShadowLooper.idleMainLooper()
  compose.waitUntil{compose.onAllNodesWithTag("try_demo").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithTag("portal_admin").assertDoesNotExist()
  compose.onNodeWithTag("portal_principal").assertExists()
  compose.onNodeWithTag("portal_teacher").assertExists()
  compose.onNodeWithTag("portal_family").assertExists()
  compose.onRoot().captureRoboImage("build/reports/admin-removal/entry_dark.png")
  compose.onNodeWithTag("try_demo").performScrollTo().performClick()
  compose.onNodeWithTag("role_select_admin").assertDoesNotExist()
  for(role in MobileRoleAccess.supportedRoles)compose.onNodeWithTag("role_select_${role.name.lowercase()}").performScrollTo().assertExists()
  compose.onRoot().captureRoboImage("build/reports/admin-removal/demo_dark.png")
 }
 @Test fun adminShellCannotRenderNestedContentEvenForFormerDestinations() {
  val repo=admin();var contentRendered=false;var signedOut=false
  compose.setContent{ScholaTheme(true){ScholaResponsiveScaffold(Screen.Students.route,repo.currentUser.value,0,{},{},{},{},{signedOut=true}){
   SideEffect{contentRendered=true}
  }}}
  compose.onNodeWithTag("unsupported_mobile_account").assertExists()
  assertFalse(contentRendered)
  compose.onNodeWithTag("unsupported_mobile_sign_out").performClick()
  assertTrue(signedOut)
 }
}
