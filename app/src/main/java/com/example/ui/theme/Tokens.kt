package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.data.model.UserRole

// Design Tokens — REVENEX ERP
// Spacing scale (4, 8, 12, 16, 24, 32)
object Spacing {
  val xs4 = 4.dp
  val sm8 = 8.dp
  val md12 = 12.dp
  val lg16 = 16.dp
  val xl24 = 24.dp
  val xxl32 = 32.dp
}

// Corner radii (12, 16, 20, 999)
object Radius {
  val md = 12.dp
  val lg = 16.dp
  val xl = 20.dp
  val pill = 999.dp
}

// Type scale (28/700, 20/600, 16/400, 13/500)
object TypeTokens {
  val sizeHero = 28.sp
  val sizeTitle = 20.sp
  val sizeBody = 16.sp
  val sizeLabel = 13.sp

  val weightHero = androidx.compose.ui.text.font.FontWeight.Bold
  val weightTitle = androidx.compose.ui.text.font.FontWeight.SemiBold
  val weightBody = androidx.compose.ui.text.font.FontWeight.Normal
  val weightLabel = androidx.compose.ui.text.font.FontWeight.Medium
}

// Per-role accent colours: student, parent, teacher, admin
object RoleAccent {
  val Student = Color(0xFF2563EB)
  val Parent = Color(0xFF0D9488)
  val Teacher = Color(0xFF7C3AED)
  val Admin = Color(0xFF475569)

  fun of(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> Admin
    UserRole.TEACHER -> Teacher
    UserRole.STUDENT -> Student
    UserRole.PARENT -> Parent
    null -> Admin
  }

  fun softContainer(role: UserRole?): Color = when (role) {
    UserRole.PRINCIPAL -> Color(0xFFE2E8F0)
    UserRole.TEACHER -> Color(0xFFEDE9FE)
    UserRole.STUDENT -> Color(0xFFDBEAFE)
    UserRole.PARENT -> Color(0xFFCCFBF1)
    null -> Color(0xFFE2E8F0)
  }
}