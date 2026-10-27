package com.example.ui.screens.operations

import com.example.data.model.UserRole
import com.example.ui.navigation.Screen

/** Presentation visibility only. Supabase remains the authorization boundary. */
enum class WorkspaceArea(val label: String) {
  PEOPLE("People"), ACADEMICS("Academics"), ATTENDANCE("Attendance"), FINANCE("Finance"),
  COMMUNICATION("Communication"), OPERATIONS("Operations"), REPORTS("Reports"), SETTINGS("Settings")
}

data class WorkspaceDestination(
  val title: String,
  val description: String,
  val area: WorkspaceArea,
  val route: String,
  val roles: Set<UserRole> = com.example.data.model.MobileRoleAccess.supportedRoles
)

object WorkspaceDirectory {
  private val administrators = setOf(UserRole.PRINCIPAL)
  private val familyAndAdministrators = administrators + setOf(UserRole.PARENT, UserRole.STUDENT)
  val destinations = listOf(
    WorkspaceDestination("Students", "Enrolment, profiles and class allocation", WorkspaceArea.PEOPLE, Screen.Students.route, administrators),
    WorkspaceDestination("Faculty", "Teacher profiles and workloads", WorkspaceArea.PEOPLE, Screen.Teachers.route, administrators),
    WorkspaceDestination("My profile", "Personal and professional details", WorkspaceArea.PEOPLE, Screen.TeacherProfile.route, setOf(UserRole.TEACHER)),
    WorkspaceDestination("Timetable", "Class schedules and periods", WorkspaceArea.ACADEMICS, Screen.Timetable.route),
    WorkspaceDestination("Assignments", "Class work, attachments and submissions", WorkspaceArea.ACADEMICS, Screen.Homework.route),
    WorkspaceDestination("Resources", "Notes, PDFs and study materials", WorkspaceArea.ACADEMICS, Screen.StudyMaterial.route),
    WorkspaceDestination("Examinations", "Exam dates and schedules", WorkspaceArea.ACADEMICS, Screen.Exams.route),
    WorkspaceDestination("Results", "Student reports and published marks", WorkspaceArea.ACADEMICS, Screen.ReportCard.route),
    WorkspaceDestination("Attendance register", "Class, date and session roll call", WorkspaceArea.ATTENDANCE, Screen.Attendance.route),
    WorkspaceDestination("Attendance overview", "Recorded class registers and exceptions", WorkspaceArea.ATTENDANCE, Screen.PrincipalAttendance.route, administrators),
    WorkspaceDestination("Leave requests", "Applications, decisions and history", WorkspaceArea.ATTENDANCE, Screen.Leaves.route, com.example.data.model.MobileRoleAccess.supportedRoles - UserRole.STUDENT),
    WorkspaceDestination("Fees", "Invoices, dues, payments and receipts", WorkspaceArea.FINANCE, Screen.Fees.route, familyAndAdministrators),
    WorkspaceDestination("Activity", "Confirmed school changes", WorkspaceArea.REPORTS, Screen.Audit.route, administrators),
    WorkspaceDestination("Reports", "School summaries and operational review", WorkspaceArea.REPORTS, Screen.Reports.route, administrators),
    WorkspaceDestination("Transport", "Routes, stops and driver contacts", WorkspaceArea.OPERATIONS, Screen.Transport.route),
    WorkspaceDestination("Library", "Book catalogue and availability", WorkspaceArea.OPERATIONS, Screen.Library.route),
    WorkspaceDestination("Inventory", "Assets, quantities and locations", WorkspaceArea.OPERATIONS, Screen.Inventory.route, administrators),
    WorkspaceDestination("Notices", "Announcements and recorded school activity", WorkspaceArea.COMMUNICATION, Screen.Notices.route),
    WorkspaceDestination("Inbox", "Your updates and unread messages", WorkspaceArea.COMMUNICATION, Screen.Notifications.route),
    WorkspaceDestination("Search", "Find records available to your account", WorkspaceArea.OPERATIONS, Screen.GlobalSearch.route),
    WorkspaceDestination("AI assistant", "School questions and guided help", WorkspaceArea.ACADEMICS, Screen.AiAssistant.route),
    WorkspaceDestination("Settings", "Appearance and signed-in account", WorkspaceArea.SETTINGS, Screen.Profile.route)
  )

  fun visible(role: UserRole, query: String = "", area: WorkspaceArea? = null) =
    destinations.filter { destination ->
      com.example.data.model.MobileRoleAccess.supports(role) && role in destination.roles && (area == null || area == destination.area) &&
        listOf(destination.title, destination.description, destination.area.label)
          .any { it.contains(query.trim(), ignoreCase = true) }
    }
}
