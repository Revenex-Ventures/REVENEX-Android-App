package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.UserRole

/** Restore a tab's saved presentation, then return Student tabs to their landing page.
 * A restored secondary stack must not make a primary-tab tap reopen a notice/detail.
 */
fun androidx.navigation.NavHostController.navigatePortalPrimary(route:String,role:UserRole) {
  if(currentBackStackEntry?.destination?.route==route)return
  navigate(route){
    popUpTo(Screen.Dashboard.route){saveState=true}
    launchSingleTop=true
    restoreState=route!=Screen.Dashboard.route
  }
  if(role==UserRole.STUDENT&&NavConfig.getBottomNavItems(role).any{it.route==route})popBackStack(route,false)
}

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
  object TeacherClasses : Screen("teacher_classes")
  object StudentLearn : Screen("student_learn")
  object StudentIdentity : Screen("student_identity")
  object StudentAssignment : Screen("student_assignment/{id}") {fun createRoute(id:String)="student_assignment/${android.net.Uri.encode(id)}"}
  object StudentNotice : Screen("student_notice/{id}") {fun createRoute(id:String)="student_notice/${android.net.Uri.encode(id)}"}
  object StudentInvoice : Screen("student_invoice/{id}") {fun createRoute(id:String)="student_invoice/${android.net.Uri.encode(id)}"}
  object StudentReport : Screen("student_report/{id}") {fun createRoute(id:String)="student_report/${android.net.Uri.encode(id)}"}
  object TeacherClass : Screen("teacher_class/{cohort}") { fun createRoute(cohort:String)="teacher_class/${android.net.Uri.encode(cohort)}" }
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
  object PrincipalApprovalDetail : Screen("principal_approval/{requestId}") {fun createRoute(id:String)="principal_approval/${android.net.Uri.encode(id)}"}
  object PrincipalSubjects : Screen("principal_subjects")
  object PrincipalCalendar : Screen("principal_calendar")
  object PrincipalAllocations : Screen("principal_allocations")
  object PrincipalClass : Screen("principal_class/{cohort}?date={date}") {
    fun createRoute(cohort:String,date:String)="principal_class/${android.net.Uri.encode(cohort)}?date=$date"
  }
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
      role==UserRole.STUDENT && route in setOf(Screen.Homework.route,Screen.StudentAssignment.route,Screen.ReportCard.route,Screen.StudentReport.route,Screen.Library.route,Screen.StudyMaterial.route,Screen.Exams.route) -> Screen.StudentLearn.route
      role==UserRole.STUDENT && route==Screen.Attendance.route -> Screen.Dashboard.route
      role==UserRole.STUDENT && route in setOf(Screen.Notices.route,Screen.StudentNotice.route) -> Screen.Notifications.route
      role==UserRole.TEACHER && route in setOf(Screen.TeacherClass.route,Screen.Students.route,Screen.StudentDetail.route,Screen.Attendance.route) -> Screen.TeacherClasses.route
      role==UserRole.TEACHER && route==Screen.ReportCard.route -> Screen.Homework.route
      route in setOf(Screen.StudentDetail.route, Screen.Teachers.route, Screen.TeacherDetail.route,Screen.PrincipalAllocations.route) &&
        role in setOf(UserRole.ADMIN, UserRole.PRINCIPAL) -> Screen.Students.route
      route in setOf(Screen.PrincipalAttendance.route,Screen.PrincipalClass.route) && role == UserRole.PRINCIPAL -> Screen.OperationsHub.route
      role in setOf(UserRole.ADMIN,UserRole.PRINCIPAL) && route in setOf(Screen.Homework.route, Screen.Timetable.route,
        Screen.Exams.route, Screen.ReportCard.route, Screen.StudyMaterial.route,Screen.PrincipalSubjects.route,Screen.PrincipalCalendar.route) -> Screen.Academics.route
      else -> Screen.OperationsHub.route
    }
  }
  fun getBottomNavItems(role: UserRole): List<BottomNavItem> {
    return when (role) {
      UserRole.PRINCIPAL -> listOf(
        BottomNavItem(Screen.Dashboard.route,"Home",Icons.Filled.Home,Icons.Outlined.Home),
        BottomNavItem(Screen.Students.route,"People",Icons.Filled.People,Icons.Outlined.People),
        BottomNavItem(Screen.Academics.route,"Academics",Icons.Filled.MenuBook,Icons.Outlined.MenuBook),
        BottomNavItem(Screen.Fees.route,"Finance",Icons.Filled.CreditCard,Icons.Outlined.CreditCard),
        BottomNavItem(Screen.OperationsHub.route,"More",Icons.Filled.MoreHoriz,Icons.Outlined.MoreHoriz)
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
        BottomNavItem(Screen.TeacherClasses.route, "Classes", Icons.Filled.Groups, Icons.Outlined.Groups),
        BottomNavItem(Screen.Homework.route, "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.Timetable.route, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem(Screen.OperationsHub.route, "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
      )
      UserRole.STUDENT -> listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.StudentLearn.route,"Learn",com.example.ui.screens.students.portal.StudentIcons.Book,com.example.ui.screens.students.portal.StudentIcons.Book),
        BottomNavItem(Screen.Timetable.route,"Schedule",com.example.ui.screens.students.portal.StudentIcons.Calendar,com.example.ui.screens.students.portal.StudentIcons.Calendar),
        BottomNavItem(Screen.Notifications.route,"Inbox",Icons.Outlined.Mail,Icons.Outlined.Mail),
        BottomNavItem(Screen.OperationsHub.route,"More",Icons.Filled.MoreHoriz,Icons.Outlined.MoreHoriz)
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
