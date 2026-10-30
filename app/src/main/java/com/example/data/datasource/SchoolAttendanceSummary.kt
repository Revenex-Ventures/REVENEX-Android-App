package com.example.data.datasource

import com.example.data.model.Student
import com.example.data.model.parseSchoolDate
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.serialization.json.*

internal data class SchoolAttendanceSummary(
  val attended: Long, val recorded: Long, val scope: String = "SESSION_TO_DATE",
  val start: String? = null, val end: String? = null
) {
  val percentage: Double get() = if (recorded == 0L) 0.0 else 100.0 * attended / recorded
}

/** Same metric for dashboard and report cards: recorded roll-call sessions, not calendar days. */
internal fun schoolAttendanceSummary(
  student: Student, attendance: List<SupabaseRow>,
  start: LocalDate? = null, end: LocalDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
): SchoolAttendanceSummary {
  val records = attendance.filter { row ->
    row.student_id == student.id && row.school_id == student.schoolId && row.session_id == student.sessionId &&
      row.status in setOf("PRESENT", "LATE", "EXCUSED", "ABSENT", "LEAVE")
  }.filter { row ->
    val date = checkNotNull(parseSchoolDate(row.attendance_date)) { "Attendance date could not be verified." }
    (start == null || date >= start) && date <= end
  }
  return SchoolAttendanceSummary(records.count { it.status in setOf("PRESENT", "LATE", "EXCUSED") }.toLong(),
    records.size.toLong(), start = start?.toString(), end = end.toString())
}

internal fun attendanceFromSnapshot(student: Student, all: Map<String, List<SupabaseRow>>, term: String = ""): SchoolAttendanceSummary {
  val summaries = all["attendance_summaries"] ?: return schoolAttendanceSummary(student, all["attendance"].orEmpty())
  val scoped = summaries.filter { it.student_id == student.id && it.school_id == student.schoolId && it.session_id == student.sessionId }
  // Legacy reports carried session attendance. A configured term explicitly narrows that scope.
  val row = scoped.singleOrNull { it.term == term } ?: scoped.singleOrNull { it.term.isEmpty() }
    ?: error("Attendance summary is incomplete. Reload before using report totals.")
  val attended = row.payload["attended"]?.jsonPrimitive?.longOrNull
  val recorded = row.payload["recorded"]?.jsonPrimitive?.longOrNull
  check(attended != null && recorded != null && attended >= 0 && attended <= recorded) { "Invalid attendance aggregate." }
  return SchoolAttendanceSummary(attended, recorded, row.payload["scope"]?.jsonPrimitive?.content ?: error("Attendance scope missing."),
    row.payload["startDate"]?.jsonPrimitive?.contentOrNull, row.payload["endDate"]?.jsonPrimitive?.contentOrNull)
}

internal fun validateAttendanceSummaryCompleteness(all:Map<String,List<SupabaseRow>>) {
  val summaries=all["attendance_summaries"]?:return
  val students=all["students"].orEmpty().map{it.id}.toSet()
  val sessionRows=summaries.filter{it.term.isEmpty()}
  check(sessionRows.size==students.size&&sessionRows.map{it.student_id}.toSet()==students) {
    "Roster changed while attendance totals were loading. Reload the complete school snapshot."
  }
}
