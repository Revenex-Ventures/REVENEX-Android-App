package com.example

import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.screens.academics.ReportCardScreen
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
@Config(sdk=[34],qualifiers="w393dp-h852dp-xhdpi")
class BrandRevisionScreensTest {
 @get:Rule val compose=createComposeRule()
 private fun capture(name:String){compose.waitForIdle();File("build/reports/brand-revision").mkdirs();compose.onRoot().captureRoboImage("build/reports/brand-revision/$name.png")}
 @Test fun familyProfileReportAndCertificateFlowsRetainBackNavigationInBothThemes(){
  val repo=ErpDataRepository.getInstance();repo.setDataMode(DataMode.DEMO);repo.switchRole(UserRole.PARENT)
  org.robolectric.shadows.ShadowLooper.idleMainLooper();compose.waitUntil{repo.currentUser.value.role==UserRole.PARENT}
  var dark by mutableStateOf(false)
  compose.setContent{ScholaTheme(dark){Surface(Modifier.fillMaxSize()){ReportCardScreen(repo)}}}
  capture("family_profile_light");compose.runOnIdle{dark=true};capture("family_profile_dark");compose.runOnIdle{dark=false}
  for((label,key) in listOf("Personal Details" to "personal_details","Academic Performance" to "academic_report","Leaving Certificate" to "leaving_certificate","Bonafide Certificate" to "bonafide_certificate")){
   compose.onNodeWithText(label).performScrollTo().performClick();capture(key+"_light")
   compose.runOnIdle{dark=true};capture(key+"_dark");compose.runOnIdle{dark=false}
   compose.onNodeWithContentDescription("Back").performClick()
  }
 }
 @Test @Config(qualifiers="w320dp-h640dp-xhdpi")
 fun narrowSearchRetainsARealCloseActionAndEmptyState(){
  val repo=ErpDataRepository.getInstance();repo.setDataMode(DataMode.DEMO)
  var closed=false
  compose.setContent{
   val d=androidx.compose.ui.platform.LocalDensity.current
   CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(d.density,1.5f)) {
    ScholaTheme(true){com.example.ui.components.GlobalCommandPaletteModal(repo,{closed=true},{},{})}
   }
  }
  compose.onNodeWithTag("command_palette_search_field").performTextInput("no-match-for-ui-verification")
  compose.onNodeWithText("No matches found").assertExists()
  compose.onNodeWithContentDescription("Close search").assertIsDisplayed().performClick()
  org.junit.Assert.assertTrue(closed)
 }

}
