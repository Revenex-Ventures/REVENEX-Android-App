package com.example

import com.example.data.datasource.*
import com.example.data.repository.SampleData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BackendRemediationTest {
  @Test fun documentInspectionRequiresExplicitServerConfirmation() {
    assertTrue(SupabaseFileStore.inspectionConfirmed("""{"validated":true,"inspection":"FORMAT_ONLY"}"""))
    for(body in listOf("{}","bad","""{"validated":true}""", """{"validated":false,"inspection":"FORMAT_ONLY"}""",
      """{"validated":true,"inspection":"CLEAN"}""")) assertFalse(SupabaseFileStore.inspectionConfirmed(body))
  }

  private val student = SampleData.initialStudents.first()
  private fun attendance(id: String, status: String, session: String = student.sessionId, date: String = "2026-10-01") =
    SupabaseRow(id, school_id = student.schoolId, session_id = session, student_id = student.id,
      status = status, attendance_date = date)

  @Test fun studentAttendanceNeverMixesAcademicSessions() {
    val rows = listOf(attendance("current", "PRESENT"), attendance("old", "ABSENT", "old_session"))
    assertEquals(100.0, summarizeSupabaseStudent(student, emptyList(), rows).attendancePercent, 0.001)
  }

  @Test fun reportingPeriodIsInclusiveAndDoesNotCountFutureOrForeignRecords() {
    val rows=listOf(attendance("before","PRESENT",date="2026-09-30"),attendance("start","PRESENT"),
      attendance("end","ABSENT",date="2026-10-02"),attendance("after","PRESENT",date="2026-10-03"),
      attendance("future","PRESENT",date="2099-01-01"),attendance("foreign","ABSENT").copy(school_id="foreign"))
    assertEquals(50.0,schoolAttendanceSummary(student,rows,LocalDate.parse("2026-10-01"),LocalDate.parse("2026-10-02")).percentage,0.001)
  }

  @Test fun configuredTermAggregateAndSessionAggregateKeepTheirOwnScope() {
    fun aggregate(term:String,attended:Int,total:Int,scope:String)=SupabaseRow("summary$term",school_id=student.schoolId,
      session_id=student.sessionId,student_id=student.id,term=term,payload=buildJsonObject{
        put("attended",attended);put("recorded",total);put("scope",scope)
      })
    val all=mapOf("attendance_summaries" to listOf(aggregate("",3,5,"SESSION_TO_DATE"),aggregate("Term 1",1,2,"TERM")))
    assertEquals(60.0,attendanceFromSnapshot(student,all).percentage,0.001)
    assertEquals(50.0,attendanceFromSnapshot(student,all,"Term 1").percentage,0.001)
    assertEquals("TERM",attendanceFromSnapshot(student,all,"Term 1").scope)
    assertEquals("SESSION_TO_DATE",attendanceFromSnapshot(student,all,"Legacy term").scope)
  }

  @Test fun integrationConfigurationNeverClaimsAVerifiedProvider() {
    assertEquals(IntegrationReadiness.LOCAL_ONLY,integrationReadiness(DataMode.DEMO,true,true).payments)
    assertEquals(IntegrationReadiness.PUBLIC_CONFIGURATION_MISSING,integrationReadiness(DataMode.SUPABASE_DEMO,false,false).payments)
    assertEquals(IntegrationReadiness.SIGN_IN_REQUIRED,integrationReadiness(DataMode.SUPABASE_DEMO,true,false).payments)
    val status=integrationReadiness(DataMode.SUPABASE_DEMO,true,true)
    assertEquals(IntegrationReadiness.NOT_VERIFIED,status.payments)
    assertEquals("OpenAI",status.assistantProvider)
  }

  @Test fun partialServerSummaryCannotPublishACompleteRoster() {
    val partial=mapOf("students" to listOf(SupabaseRow("one"),SupabaseRow("two")),
      "attendance_summaries" to listOf(SupabaseRow("summary",student_id="one")))
    assertTrue(runCatching{validateAttendanceSummaryCompleteness(partial)}.isFailure)
  }

  @Test fun resultBatchRequiresEveryRosterSubjectAndPreservesObservedVersions() {
    val scope=SupabaseRow("batch",school_id="s",session_id="a",section_id="class",term="Exam",
      eligible_student_ids=listOf("one","two"),required_subjects=listOf("Math"),version=3)
    val marks=listOf("one","two").mapIndexed{i,id->SupabaseRow("mark$id",school_id="s",session_id="a",section_id="class",
      student_id=id,term="Exam",subject="Math",version=i+2)}
    assertTrue(runCatching{observedResultBatch("one","Exam",listOf(scope),marks.take(1))}.isFailure)
    val result=observedResultBatch("one","Exam",listOf(scope),marks)
    assertEquals(3,result.first.version)
    assertEquals(listOf(2,3),result.second.map{it.jsonObject.getValue("version").jsonPrimitive.int})
  }

  @Test fun dashboardAndReportUseTheSameStructuredAttendance() = runBlocking {
    val source = SupabaseDataSource()
    val field = SupabaseDataSource::class.java.getDeclaredField("rows").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    val state = field.get(source) as kotlinx.coroutines.flow.MutableStateFlow<Map<String, List<SupabaseRow>>>
    val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    state.value = mapOf(
      "students" to listOf(SupabaseRow(student.id, school_id=student.schoolId, session_id=student.sessionId,
        payload=Json.parseToJsonElement(moshi.adapter(com.example.data.model.Student::class.java).toJson(student)).jsonObject)),
      "attendance" to listOf(attendance("a", "PRESENT"), attendance("b", "ABSENT")),
      "marks" to listOf(SupabaseRow("mark", school_id=student.schoolId, session_id=student.sessionId,
        student_id=student.id, term="Term 1", subject="Math", obtained=80, maximum=100))
    )
    val dashboard = source.getStudentsFlow().first().single()
    val report = source.getReportCardsFlow().first().single()
    assertEquals(50.0, dashboard.attendancePercent, 0.001)
    assertEquals(dashboard.attendancePercent, report.attendanceRate, 0.001)
    assertEquals(student.sessionId, report.sessionId)
  }
}
