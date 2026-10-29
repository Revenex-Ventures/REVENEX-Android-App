package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.example.ui.navigation.*
import com.example.ui.screens.principal.PrincipalBottomNavigation
import com.example.ui.screens.teachers.TeacherNavigation
import com.example.ui.screens.students.portal.StudentNavigation
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w360dp-h500dp-xhdpi")
class DarkDockSpacingTest {
 @get:Rule val compose=createComposeRule()
 @Test fun allSelectedDestinationsContainTheirLabelsAndKeepNavigation()=verify(1f)
 @Test @Config(sdk=[34],qualifiers="w320dp-h600dp-xhdpi")
 fun narrowLargeTextWrapsRowsWithoutClippingAnyOption()=verify(1.5f)
 private fun verify(scale:Float){
  var role by mutableStateOf(UserRole.PRINCIPAL);var route by mutableStateOf(Screen.Dashboard.route)
  var actual=""
  compose.setContent{
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,scale)){
    ScholaTheme(true,principal=role==UserRole.PRINCIPAL,teacher=role==UserRole.TEACHER,student=role==UserRole.STUDENT){
     Column(Modifier.fillMaxWidth().testTag("dock_capture")){
      when(role){UserRole.PRINCIPAL->PrincipalBottomNavigation(route,{actual=it;route=it});UserRole.TEACHER->TeacherNavigation(route,{actual=it;route=it});else->StudentNavigation(route,{actual=it;route=NavConfig.selectedPrimaryRoute(role,it)?:it})}
     }
    }
   }
  }
  File("build/reports/dock-spacing").mkdirs()
  for(portal in listOf(UserRole.PRINCIPAL,UserRole.TEACHER,UserRole.STUDENT)){
   compose.runOnIdle{role=portal;route=Screen.Dashboard.route};compose.waitForIdle()
   val destinations=when(portal){
    UserRole.PRINCIPAL->listOf("Home" to Screen.Dashboard.route,"People" to Screen.Students.route,"Academics" to Screen.Academics.route,"Reports" to Screen.Reports.route,"You" to Screen.Profile.route)
    UserRole.TEACHER->NavConfig.getBottomNavItems(portal).map{it.label to it.route}
    else->NavConfig.getBottomNavItems(portal).map{(if(it.route==Screen.OperationsHub.route)"You"else it.label) to it.route}
   }
   val prefix=portal.name.lowercase()+"_dark_nav_"
   for((label,target)in destinations){
    compose.runOnIdle{route=target};compose.waitForIdle()
    val node=compose.onNodeWithTag(prefix+label)
    node.assertIsSelected().performClick()
    assertEquals(if(portal==UserRole.STUDENT&&label=="You")Screen.Profile.route else target,actual)
    val pill=node.getUnclippedBoundsInRoot()
    assertTrue(pill.height>=48.dp)
    for((name,_)in destinations){
     val text=compose.onNodeWithTag(prefix+name+"_label",useUnmergedTree=true)
     val results=mutableListOf<TextLayoutResult>()
     text.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult){it(results)}
     if(results.isEmpty()||results.first().hasVisualOverflow){
      println("Dock overflow $portal $name scale=$scale results=${results.size} layout=${results.firstOrNull()?.size} width=${results.firstOrNull()?.didOverflowWidth} height=${results.firstOrNull()?.didOverflowHeight} text=${text.getUnclippedBoundsInRoot()} parent=${compose.onNodeWithTag(prefix+name).getUnclippedBoundsInRoot()}")
      compose.onNodeWithTag("dock_capture").captureRoboImage("build/reports/dock-spacing/failure_${scale}.png")
     }
     assertTrue("Clipped label: $portal $name",results.isNotEmpty()&&!results.first().hasVisualOverflow)
     val bounds=text.getUnclippedBoundsInRoot();val parent=compose.onNodeWithTag(prefix+name).getUnclippedBoundsInRoot()
     assertTrue(bounds.left>=parent.left&&bounds.right<=parent.right&&bounds.top>=parent.top&&bounds.bottom<=parent.bottom)
    }
    compose.onNodeWithTag("dock_capture").captureRoboImage("build/reports/dock-spacing/${portal.name.lowercase()}_${label.lowercase()}_${scale}.png")
   }
  }
 }
}
