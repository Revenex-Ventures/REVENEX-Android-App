package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.UserRole

sealed class Screen(val route: String) {
  object Auth : Screen("auth")
  object Loading : Screen("loading")
  object Dashboard : Screen("dashboard")
  object Profile : Screen("profile")
  object Students : Screen("students")
  object StudentDetail : Screen("student_detail/{studentId}") {
    fun createRoute(studentId: String) = "student_detail/$studentId"
  }
  object Teachers : Screen("teachers")
  object TeacherDetail : Screen("teacher_detail/{teacherId}") {
    fun createRoute(teacherId: String) = "teacher_detail/$teacherId"
  }
  object TeacherProfile : Screen("teacher_profile")
  object Attendance : Screen("attendance?classGrade={classGrade}&division={division}") {
    fun createRoute(classGrade: String, division: String) = "attendance?classGrade=$classGrade&division=$division"
  }
  object Fees : Screen("fees")
  object Timetable : Screen("timetable")
  object Exams : Screen("exams")
  object ReportCard : Screen("report_card")
  object Homework : Screen("homework")
  object StudyMaterial : Screen("study_material")
  object Notices : Screen("notices")
  object Notifications : Screen("notifications")
  object Leaves : Screen("leaves")
  object Transport : Screen("transport")
  object Library : Screen("library")
  object Inventory : Screen("inventory")
  object Reports : Screen("reports")
  object AiAssistant : Screen("ai_assistant")
  object GlobalSearch : Screen("global_search")
  object OperationsHub : Screen("operations_hub")
  object Academics : Screen("academic_hub")
  object Audit : Screen("school_activity")
  object PrincipalAttendance : Screen("principal_attendance")
}

data class BottomNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

object NavConfig {
  /** Presentation selection only; authorization remains with the backend. */
  fun selectedPrimaryRoute(role: UserRole, route: String?): String? {
    val primary = getBottomNavItems(role).map { it.route }
    if (route in primary) return route
    return when {
      route == Screen.Auth.route || route == null -> route
      route in setOf(Screen.StudentDetail.route, Screen.Teachers.route, Screen.TeacherDetail.route) &&
        role in setOf(UserRole.ADMIN, UserRole.PRINCIPAL) -> Screen.Students.route
      route == Screen.PrincipalAttendance.route && role == UserRole.PRINCIPAL -> Screen.Attendance.route
      role == UserRole.ADMIN && route in setOf(Screen.Homework.route, Screen.Timetable.route,
        Screen.Exams.route, Screen.ReportCard.route, Screen.StudyMaterial.route) -> Screen.Academics.route
      else -> Screen.OperationsHub.route
    }
  }
  fun getBottomNavItems(role: UserRole): List<BottomNavItem> {
    return when (role) {
      UserRole.PRINCIPAL -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Students.route, "Students", Icons.Filled.People, Icons.Outlined.People),
        BottomNavItem(Screen.Attendance.route, "Attendance", Icons.Filled.HowToReg, Icons.Outlined.HowToReg),
        BottomNavItem(Screen.Fees.route, "Fees", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Apps, Icons.Outlined.Apps)
      )
      UserRole.ADMIN -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Students.route, "People", Icons.Filled.Groups, Icons.Outlined.Groups),
        BottomNavItem(Screen.Academics.route, "Academics", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Fees.route, "Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Apps, Icons.Outlined.Apps)
      )
      UserRole.TEACHER -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Attendance.route, "Attendance", Icons.Filled.FactCheck, Icons.Outlined.FactCheck),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.Timetable.route, "Timetable", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Menu, Icons.Outlined.Menu)
      )
      UserRole.STUDENT -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Timetable.route, "Timetable", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Menu, Icons.Outlined.Menu),
        BottomNavItem(Screen.ReportCard.route, "Results", Icons.Filled.Person, Icons.Outlined.Person)
      )
      // Shared navigation presentation; parent identity and linked-child scope remain distinct.
      UserRole.PARENT -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Timetable.route, "Timetable", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Menu, Icons.Outlined.Menu),
        BottomNavItem(Screen.ReportCard.route, "Results", Icons.Filled.Person, Icons.Outlined.Person)
      )
    }
  }
}
