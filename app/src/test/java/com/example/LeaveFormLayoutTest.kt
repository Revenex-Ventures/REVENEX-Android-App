package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.example.data.model.UserRole
import com.example.ui.components.ApplyLeaveDialog
import com.example.ui.theme.ScholaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Keep native text measurement in its own sandbox instead of changing renderers
// between methods of the confirmed-write interaction test.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w360dp-h800dp-xhdpi")
class LeaveFormLayoutTest {
 @get:Rule val compose=createComposeRule()
 @Test fun leaveTypeLabelsRemainSingleLineOnNarrowPhone() {
  compose.setContent {
   CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,1.3f)) {
    ScholaTheme(false,teacher=true) {
     ApplyLeaveDialog("Synthetic teacher",UserRole.TEACHER,{}, {_,_,_,_,_->},onSubmitLeave={_,_,_,_,_->})
    }
   }
  }
  listOf("Medical","Casual","Family Event","Sports").forEach { label ->
   val layouts=mutableListOf<TextLayoutResult>()
   compose.onNodeWithText(label).performScrollTo().performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
   assertTrue("Missing text layout for $label",layouts.isNotEmpty())
   assertEquals("Leave type $label must not wrap into a vertical label",1,layouts.single().lineCount)
  }
 }
}
