package com.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.theme.ScholaTheme
import com.example.ui.theme.SemanticColors
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class ThemeContrastTest {
  @get:Rule val compose = createComposeRule()
  @Test fun textPairsMeetNormalTextContrastInBothPalettes() {
    var dark by mutableStateOf(false)
    var pairs = emptyList<Pair<Color,Color>>()
    compose.setContent { ScholaTheme(dark) {
      val c = MaterialTheme.colorScheme
      pairs = listOf(c.primary to c.onPrimary, c.primaryContainer to c.onPrimaryContainer,
        c.secondary to c.onSecondary, c.background to c.onBackground, c.surface to c.onSurface,
        c.surfaceVariant to c.onSurfaceVariant, c.errorContainer to c.onErrorContainer,
        SemanticColors.successContainer to SemanticColors.onSuccess,
        SemanticColors.warningContainer to SemanticColors.onWarning,
        SemanticColors.lavenderContainer to SemanticColors.onLavender,
        SemanticColors.peachContainer to SemanticColors.onPeach)
    } }
    listOf(false,true).forEach { mode ->
      compose.runOnIdle { dark=mode }
      compose.runOnIdle {
        pairs.forEach { (background,foreground) ->
          val a=background.luminance(); val b=foreground.luminance()
          val ratio=(maxOf(a,b)+0.05f)/(minOf(a,b)+0.05f)
          assertTrue("Contrast $ratio in ${if(mode) "dark" else "light"} palette",ratio>=4.5f)
        }
      }
    }
  }
}
