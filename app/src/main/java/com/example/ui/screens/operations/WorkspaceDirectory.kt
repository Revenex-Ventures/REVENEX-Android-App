package com.example.ui.screens.operations

import com.example.data.model.UserRole
import com.example.ui.navigation.Screen

/** Presentation visibility only. Supabase remains the authorization boundary. */
enum class WorkspaceArea(val label: String) {
  PEOPLE("People & admissions"), ACADEMICS("Academics"),
  ATTENDANCE("Attendance & leave"), FINANCE("Finance"),
  OPERATIONS("School operations"), ACCOUNT("Communication & account")
}

data class WorkspaceDestination(
  val title: String,
  val description: String,
  val area: WorkspaceArea,
  val route: String,
  val roles: Set<UserRole> = UserRole.entries.toSet()
)

object WorkspaceDirectory {
  private val administrators = setOf(UserRole.PRINCIPAL, UserRole.ADMIN)
  private val familyAndAdministrators = administrators + setOf(UserRole.PARENT, UserRole.STUDENT)
  val destinations = listOf(
    WorkspaceDestination("Students & admissions", "Enrolment, profiles and class allocation", WorkspaceArea.PEOPLE, Screen.Students.route, administrators),
    WorkspaceDestination("Faculty & staff", "Teacher profiles and workloads", WorkspaceArea.PEOPLE, Screen.Teachers.route, administrators),
    WorkspaceDestination("My faculty profile", "Personal and professional details", WorkspaceArea.PEOPLE, Screen.TeacherProfile.route, setOf(UserRole.TEACHER)),
    WorkspaceDestination("Timetable", "Class schedules and periods", WorkspaceArea.ACADEMICS, Screen.Timetable.route),
    WorkspaceDestination("Assignments", "Class work, attachments and submissions", WorkspaceArea.ACADEMICS, Screen.Homework.route),
    WorkspaceDestination("Learning resources", "Notes, PDFs and study materials", WorkspaceArea.ACADEMICS, Screen.StudyMaterial.route),
    WorkspaceDestination("Examinations", "Exam dates and schedules", WorkspaceArea.ACADEMICS, Screen.Exams.route),
    WorkspaceDestination("Results", "Student reports and published marks", WorkspaceArea.ACADEMICS, Screen.ReportCard.route),
    WorkspaceDestination("Attendance register", "Class, date and session roll call", WorkspaceArea.ATTENDANCE, Screen.Attendance.route),
    WorkspaceDestination("Attendance overview", "Recorded class registers and exceptions", WorkspaceArea.ATTENDANCE, Screen.PrincipalAttendance.route, administrators),
    WorkspaceDestination("Leave requests", "Applications, decisions and history", WorkspaceArea.ATTENDANCE, Screen.Leaves.route, UserRole.entries.toSet() - UserRole.STUDENT),
    WorkspaceDestination("Fee accounts", "Invoices, dues, payments and receipts", WorkspaceArea.FINANCE, Screen.Fees.route, familyAndAdministrators),
    WorkspaceDestination("Reports", "School summaries and operational review", WorkspaceArea.OPERATIONS, Screen.Reports.route, administrators),
    WorkspaceDestination("Transport", "Routes, stops and driver contacts", WorkspaceArea.OPERATIONS, Screen.Transport.route),
    WorkspaceDestination("Library", "Book catalogue and availability", WorkspaceArea.OPERATIONS, Screen.Library.route),
    WorkspaceDestination("Inventory", "Assets, quantities and locations", WorkspaceArea.OPERATIONS, Screen.Inventory.route, administrators),
    WorkspaceDestination("Notices & audit", "Announcements and recorded school activity", WorkspaceArea.ACCOUNT, Screen.Notices.route),
    WorkspaceDestination("Notification inbox", "Your updates and unread messages", WorkspaceArea.ACCOUNT, Screen.Notifications.route),
    WorkspaceDestination("Search", "Find records available to your account", WorkspaceArea.ACCOUNT, Screen.GlobalSearch.route),
    WorkspaceDestination("AI assistant", "School questions and guided help", WorkspaceArea.ACCOUNT, Screen.AiAssistant.route),
    WorkspaceDestination("Settings & profile", "Appearance and signed-in account", WorkspaceArea.ACCOUNT, Screen.Profile.route)
  )

  fun visible(role: UserRole, query: String = "", area: WorkspaceArea? = null) =
    destinations.filter { destination ->
      role in destination.roles && (area == null || area == destination.area) &&
        listOf(destination.title, destination.description, destination.area.label)
          .any { it.contains(query.trim(), ignoreCase = true) }
    }
}
