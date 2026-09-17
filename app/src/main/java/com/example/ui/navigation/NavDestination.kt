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
  object PrincipalAttendance : Screen("principal_attendance")
}

data class BottomNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

object NavConfig {
  fun getBottomNavItems(role: UserRole): List<BottomNavItem> {
    return when (role) {
      UserRole.PRINCIPAL -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        BottomNavItem(Screen.Students.route, "Students", Icons.Filled.People, Icons.Outlined.People),
        BottomNavItem(Screen.Attendance.route, "Attendance", Icons.Filled.HowToReg, Icons.Outlined.HowToReg),
        BottomNavItem(Screen.Fees.route, "Fees", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        BottomNavItem(Screen.OperationsHub.route, "ERP Hub", Icons.Filled.Apps, Icons.Outlined.Apps)
      )
      UserRole.TEACHER -> listOf(
        BottomNavItem(Screen.Dashboard.route, "My Class", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        BottomNavItem(Screen.Attendance.route, "Attendance", Icons.Filled.FactCheck, Icons.Outlined.FactCheck),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.Timetable.route, "Timetable", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.Menu, Icons.Outlined.Menu),
        BottomNavItem(Screen.TeacherProfile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
      )
      UserRole.STUDENT -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Timetable.route, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.OperationsHub.route, "Resources", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        BottomNavItem(Screen.ReportCard.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
      )
      // Parent/Parent-Portal is folded into the Student Portal: a parent accessing via a
      // guardian link lands in the Student portal. There is no 4th portal.
      UserRole.PARENT -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Timetable.route, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.Homework.route, "Homework", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.OperationsHub.route, "Resources", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        BottomNavItem(Screen.ReportCard.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
      )
    }
  }
}
