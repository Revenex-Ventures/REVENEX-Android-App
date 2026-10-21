package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Functional palettes: readable pairs, deliberately coordinated for each appearance. */
object SemanticColors {
  private val dark: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.3f
  val successContainer: Color @Composable get() = if (dark) Color(0xFF214838) else Color(0xFFDDF1E5)
  val onSuccess: Color @Composable get() = if (dark) Color(0xFFA9E1BD) else Color(0xFF245C3C)
  val warningContainer: Color @Composable get() = if (dark) Color(0xFF4B3C24) else Color(0xFFFFEFD2)
  val onWarning: Color @Composable get() = if (dark) Color(0xFFF1D29D) else Color(0xFF725019)
  val dangerContainer: Color @Composable get() = MaterialTheme.colorScheme.errorContainer
  val onDanger: Color @Composable get() = MaterialTheme.colorScheme.onErrorContainer
  val lavenderContainer: Color @Composable get() = if (dark) Color(0xFF3B334B) else Color(0xFFEDE8F7)
  val onLavender: Color @Composable get() = if (dark) Color(0xFFD9C8EE) else Color(0xFF514365)
  val peachContainer: Color @Composable get() = if (dark) Color(0xFF4D382D) else Color(0xFFFBE7DB)
  val onPeach: Color @Composable get() = if (dark) Color(0xFFF0CAB3) else Color(0xFF724833)
}
