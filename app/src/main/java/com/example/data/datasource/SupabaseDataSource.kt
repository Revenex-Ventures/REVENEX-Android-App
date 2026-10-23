package com.example.data.datasource

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.UUID

@Serializable
data class SupabaseRow(
  val id: String,
  val school_id: String = "",
  val session_id: String = "",
  val section_id: String? = null,
  val student_id: String? = null,
  val profile_id: String? = null,
  val payload: JsonObject = JsonObject(emptyMap()),
  val role: String = "",
  val name: String = "",
  val email: String = "",
  val grade: String = "",
  val class_id: String = "",
  val division: String = "",
  val term: String = "",
  val subject: String = "",
  val obtained: Int = 0,
  val maximum: Int = 100,
  val published: Boolean = false,
  val attendance_date: String = "",
  val session_slot: String = "Morning Roll Call",
  val status: String = "",
  val title: String = "",
  val message: String = "",
  val category: String = "SYSTEM",
  val is_read: Boolean = false,
  val created_at: String = "",
  val assignment_id: String = "",
  val completed: Boolean = false,
  val action: String = "",
  val table_name: String = "",
  val record_id: String = ""
)

data class BackendSyncState(val busy: Boolean = false, val message: String = "", val failed: Boolean = false)

/** Derive summaries only from rows already authorized by PostgreSQL RLS. */
internal fun summarizeSupabaseStudent(student: Student, fees: List<FeeRecord>, attendance: List<SupabaseRow>): Student {
  val permittedFees=fees.filter { it.studentId==student.id }
  val records=attendance.filter { it.student_id==student.id && it.status in setOf("PRESENT","LATE","ABSENT") }
  return student.copy(
    feeSummaryAvailable=permittedFees.isNotEmpty(),
    feePendingAmount=permittedFees.sumOf { it.pendingAmount },
    feeStatus=permittedFees.firstOrNull { it.status==FeeStatus.OVERDUE }?.status
      ?: permittedFees.firstOrNull { it.pendingAmount>0 }?.status ?: FeeStatus.PAID,
    attendancePercent=if(records.isEmpty()) 0.0 else 100.0*records.count { it.status!="ABSENT" }/records.size,
    rank=0,gpa=0.0
  )
}

/** No sample fallback. PostgreSQL RLS, not these filters, is the authorization boundary. */
class SupabaseDataSource : ErpDataSource {
  private val client get() = SupabaseProvider.client
  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val rows = MutableStateFlow<Map<String,List<SupabaseRow>>>(emptyMap())
  private val user = MutableStateFlow(signedOutUser())
  val syncState = MutableStateFlow(BackendSyncState())
  private var liveJob: Job? = null
  private var authenticated = false
  private val lifecycleLock=Mutex()
  private val sessionRevision=AtomicLong()
  private val tables = listOf("students","teachers","classes","sections","timetables","notices","attendance",
    "assignments","assignment_submissions","marks","exams","fee_records","study_materials","leave_requests",
    "events","transport_routes","library_books","inventory","notifications","audit_logs")

  private fun requireSession() { check(authenticated) { "Sign in to the Supabase demo first." } }
  private inline fun <reified T:Any> models(table: String): Flow<List<T>> = rows.map { all ->
    all[table].orEmpty().map { row -> decode<T>(row) }
  }
  private inline fun <reified T:Any> decode(row: SupabaseRow): T {
    val data = row.payload.toMutableMap().apply {
      put("id",JsonPrimitive(row.id)); put("schoolId",JsonPrimitive(row.school_id)); put("sessionId",JsonPrimitive(row.session_id))
      if (T::class==Student::class) {
        // Mandatory legacy model fields; real summaries are derived separately.
        put("feeStatus",JsonPrimitive("PENDING")); put("feePendingAmount",JsonPrimitive(0L))
        put("feeSummaryAvailable",JsonPrimitive(false))
      }
    }
    return requireNotNull(moshi.adapter(T::class.java).fromJson(JsonObject(data).toString()))
  }
  private fun table(name: String) = rows.value[name].orEmpty()
  suspend fun refreshVerifiedPaymentData() {
    requireSession()
    listOf("fee_records","notifications","audit_logs").forEach { refresh(it) }
  }
  private suspend fun refresh(name: String) {
    val revision=sessionRevision.get()
    val result = client.from(name).select().decodeList<SupabaseRow>()
    rows.update { previous -> if(sessionRevision.get()==revision) previous+(name to result) else previous }
  }
  suspend fun authenticate(email: String, password: String) = lifecycleLock.withLock {
    authenticateSession(email,password)
  }
  @OptIn(SupabaseExperimental::class)
  private suspend fun authenticateSession(email: String, password: String) {
    SupabaseProvider.configuration.validate()
    liveJob?.cancelAndJoin()
    sessionRevision.incrementAndGet()
    rows.value=emptyMap(); authenticated=false;user.value=signedOutUser();activeSession=""
    runCatching { client.realtime.removeAllChannels() }
    client.auth.clearSession()
    syncState.value=BackendSyncState(true,"Signing in and loading school data…")
    var signInStage="auth"
    try {
      client.auth.signInWith(Email) { this.email=email.trim(); this.password=password }
      val uid = requireNotNull(client.auth.currentUserOrNull()).id
      signInStage="profile"
      val profile = client.from("profiles").select { filter { eq("id",uid) } }.decodeList<SupabaseRow>().singleOrNull() ?: error("School profile not provisioned")
      signInStage="role"
      // Additional enum roles exist in SQL, but no dashboard is fabricated for them.
      val role = UserRole.entries.firstOrNull { it.name==profile.role }
        ?: error("This account role does not have a supported demo dashboard.")
      user.value=UserProfile(uid,profile.name,profile.email,role,role.displayName,"",profile.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),schoolId=profile.school_id)
      activeSession=profile.session_id
      tables.forEach { signInStage=it; refresh(it) }
      val students=table("students")
      val own = students.firstOrNull { it.profile_id==uid } ?: if(role==UserRole.PARENT) students.firstOrNull() else null
      val section = table("sections").firstOrNull { it.id==own?.section_id }
        ?: if(role==UserRole.TEACHER) table("sections").firstOrNull() else null
      val grade = table("classes").firstOrNull { it.id==section?.class_id }?.grade
      user.value=user.value.copy(associatedStudentId=own?.id.orEmpty(),
        associatedClass=if(grade!=null) "$grade-${section?.division}" else "",
        associatedChildNames=if(role==UserRole.PARENT) students.map { it.id } else emptyList())
      authenticated=true
      syncState.value=BackendSyncState(message="School data loaded. Connecting live updates…")
      val liveRevision=sessionRevision.get()
      val ready=java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
      val interrupted=java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
      liveJob=CoroutineScope(Dispatchers.IO+SupervisorJob()).launch {
        // Realtime is a hint, not a durable inbox. Re-read authorized rows after
        // interrupted delivery; this child job is cancelled on every identity change.
        launch {
          while (isActive && sessionRevision.get()==liveRevision) {
            delay(30_000)
            try {
              listOf("notifications","assignments","attendance","marks","notices","fee_records").forEach { refresh(it) }
              if (sessionRevision.get()==liveRevision && !syncState.value.busy) {
                syncState.value=BackendSyncState(message="School inbox and task data reconciled")
              }
            } catch (e: Exception) {
              if (e is CancellationException) throw e
              if (sessionRevision.get()==liveRevision) syncState.value=BackendSyncState(
                message="Unable to refresh school updates. Displayed data may be stale; retrying automatically.",failed=true)
            }
          }
        }
        tables.forEach { name ->
          launch {
            client.from(name).selectAsFlow(SupabaseRow::id)
              .retryWhen { _, attempt ->
                if(sessionRevision.get()!=liveRevision) return@retryWhen false
                interrupted.add(name)
                syncState.value=BackendSyncState(message="Live sync interrupted. Retrying; displayed data may be stale. Reopen Supabase demo if access has changed.",failed=true)
                delay(minOf(30000L,1000L*(attempt+1))); true
              }.collect { updated ->
                if(sessionRevision.get()!=liveRevision) return@collect
                rows.update { previous -> if(sessionRevision.get()==liveRevision) previous+(name to updated) else previous }
                ready.add(name)
                val recovered=interrupted.remove(name)
                if(ready.size==tables.size && interrupted.isEmpty() && !syncState.value.busy &&
                  (recovered || syncState.value.message.contains("Connecting live updates"))) {
                  syncState.value=BackendSyncState(message="School data loaded • in-app updates enabled")
                }
              }
          }
        }
      }
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      liveJob?.cancel()
      liveJob = null
      authenticated = false
      rows.value = emptyMap()
      user.value = signedOutUser()
      activeSession = ""
      runCatching { client.realtime.removeAllChannels() }
      runCatching { client.auth.clearSession() }
      syncState.value = BackendSyncState(message = supabaseSignInFailure(signInStage,e), failed = true)
      throw IllegalStateException(syncState.value.message)
    }
  }
  private var activeSession=""
  private fun boundary(id: String, sectionId: String?=null, studentId: String?=null) = buildJsonObject {
    put("id",id);put("school_id",user.value.schoolId);put("session_id",activeSession)
    sectionId?.let { put("section_id",it) };studentId?.let { put("student_id",it) }
  }
  private fun section(grade: String, division: String): String {
    val classId=table("classes").firstOrNull { it.grade==grade }?.id
    return table("sections").firstOrNull { it.class_id==classId && it.division==division }?.id
      ?: error("No accessible class section matches this selection.")
  }
  private inline fun <reified T:Any> payload(value:T): JsonElement = Json.parseToJsonElement(moshi.adapter(T::class.java).toJson(value))
  private suspend fun write(table: String, body: JsonObject) {
    requireSession();val revision=sessionRevision.get()
    syncState.value=BackendSyncState(true,"Saving to school database…")
    try {
      client.from(table).upsert(body)
      if(sessionRevision.get()!=revision) return
      refresh(table)
      syncState.value=BackendSyncState(message="Saved to school database")
    } catch(e:Exception) {
      if(e is CancellationException) throw e
      if(sessionRevision.get()!=revision) return
      syncState.value=BackendSyncState(message="Save failed. No local success was assumed. Check access and network, then retry.",failed=true)
      throw IllegalStateException(syncState.value.message)
    }
  }
  private suspend inline fun <reified T:Any> save(table:String,id:String,value:T,sectionId:String?=null) {
    write(table,JsonObject(boundary(id,sectionId)+("payload" to payload(value))))
  }
  override fun isProductionMode()=false
  override fun getCurrentUserFlow():Flow<UserProfile> = user
  override suspend fun setCurrentUser(user:UserProfile):Unit=error("Cloud identities can only be established by Supabase Auth.")
  override suspend fun switchRole(role:UserRole):Unit=error("Cloud roles are assigned by the school operator.")
  override suspend fun login(email:String,role:UserRole,name:String):Unit=error("Use your Supabase email and password.")
  override suspend fun logout() = lifecycleLock.withLock {
    sessionRevision.incrementAndGet()
    liveJob?.cancelAndJoin();liveJob=null;authenticated=false;rows.value=emptyMap();user.value=signedOutUser();activeSession=""
    syncState.value=BackendSyncState()
    runCatching { client.realtime.removeAllChannels() }
    runCatching { withTimeout(5000) { client.auth.signOut(io.github.jan.supabase.auth.SignOutScope.LOCAL) } }
    // Always erase the in-memory credential even if the network logout fails.
    runCatching { client.auth.clearSession() }
    Unit
  }
  override fun getStudentsFlow():Flow<List<Student>> = rows.map { all ->
    val fees=all["fee_records"].orEmpty().map { decode<FeeRecord>(it) }
    all["students"].orEmpty().map { summarizeSupabaseStudent(decode<Student>(it),fees,all["attendance"].orEmpty()) }
  }
  override fun getTeachersFlow()=models<Teacher>("teachers")
  override suspend fun addStudent(student:Student) { enrollStudent(student,0L) }
  suspend fun enrollStudent(student:Student,feeAmount:Long) {
    requireSession()
    require(user.value.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) { "Only the principal or school administrator can enroll students." }
    require(feeAmount in 0L..1000000000L) { "Invalid fee amount." }
    syncState.value=BackendSyncState(true,"Saving enrollment to Supabase�")
    try {
      client.postgrest.rpc("enroll_student",buildJsonObject {
        put("p_id",student.id);put("p_section",section(student.classGrade,student.division))
        put("p_payload",payload(student));put("p_fee_amount",feeAmount)
      })
      listOf("students","fee_records","notifications","audit_logs").forEach { refresh(it) }
      syncState.value=BackendSyncState(message="Student enrolled in school database. School inbox updated.")
    } catch(e:Exception) {
      if(e is CancellationException) throw e
      syncState.value=BackendSyncState(message="Enrollment could not be confirmed. Check the student directory before retrying; verify class, duplicate admission number, access and migration 7.",failed=true)
      throw IllegalStateException(syncState.value.message)
    }
  }
  override suspend fun updateStudent(student:Student):Unit=error("Student identity changes are operator-managed in this demo.")
  override suspend fun deleteStudent(studentId:String):Unit=error("Student deletion is disabled in this demo.")
  override suspend fun addTeacher(teacher:Teacher):Unit=error("Teacher provisioning is operator-managed in this demo.")
  override suspend fun updateTeacher(teacher:Teacher):Unit=error("Teacher identity changes are operator-managed in this demo.")
  override fun getFeeRecordsFlow()=models<FeeRecord>("fee_records")
  override suspend fun processFeePayment(studentId:String,amountPaid:Long,paymentMethod:String,feeHead:String,razorpayPaymentId:String?,razorpayOrderId:String?,razorpaySignature:String?):FeePaymentTransaction = error("Payments are disabled. Demo fees are read-only.")
  override fun getClassAttendanceFlow():Flow<Map<String,ClassAttendanceRecord>> = rows.map { all ->
    all["attendance"].orEmpty().groupBy { "${it.section_id}_${it.attendance_date}_${it.session_slot}" }.mapValues { (_,records) ->
      val st=records.map { record ->
        val student=all["students"].orEmpty().firstOrNull { it.id==record.student_id }?.let { decode<Student>(it) }
        StudentAttendance(record.student_id!!,student?.name.orEmpty(),student?.rollNumber ?: 0,
          AttendanceStatus.valueOf(record.status),record.payload["remark"]?.jsonPrimitive?.content.orEmpty())
      }
      val first=records.first();val student=all["students"].orEmpty().firstOrNull { it.id==first.student_id }?.let { decode<Student>(it) }
      ClassAttendanceRecord(first.id,student?.classGrade.orEmpty(),student?.division.orEmpty(),date=first.attendance_date,sessionSlot=first.session_slot,totalStudents=st.size,
        presentCount=st.count { it.status==AttendanceStatus.PRESENT },absentCount=st.count { it.status==AttendanceStatus.ABSENT },lateCount=st.count { it.status==AttendanceStatus.LATE },
        excusedCount=st.count { it.status==AttendanceStatus.EXCUSED },leaveCount=st.count { it.status==AttendanceStatus.LEAVE },markedBy="School faculty",studentList=st,schoolId=first.school_id,sessionId=first.session_id)
    }
  }
  override suspend fun saveClassAttendance(classGrade:String,division:String,date:String,markedBy:String,studentList:List<StudentAttendance>) {
    saveRollCall(classGrade,division,date,studentList,"Morning Roll Call")
  }
  suspend fun saveRollCall(classGrade:String,division:String,date:String,studentList:List<StudentAttendance>,slot:String) {
    requireSession();val sec=section(classGrade,division)
    require(studentList.isNotEmpty()) { "Select a populated class before saving attendance." }
    // One PostgREST statement makes the roll call atomic.
    val batch=studentList.map { st -> JsonObject(boundary("${st.studentId}_${activeSession}_${date}_$slot",sec,st.studentId)+mapOf(
      "attendance_date" to JsonPrimitive(date),"session_slot" to JsonPrimitive(slot),"status" to JsonPrimitive(st.status.name),"payload" to payload(st))) }
    syncState.value=BackendSyncState(true,"Saving attendance…")
    try { client.from("attendance").upsert(batch);refresh("attendance");syncState.value=BackendSyncState(message="Attendance saved") }
    catch(e:Exception) { if(e is CancellationException) throw e;syncState.value=BackendSyncState(message="Attendance was not saved. Check access and network.",failed=true);throw IllegalStateException(syncState.value.message) }
  }
  override fun getAssignmentsFlow():Flow<List<HomeworkAssignment>> = rows.map { all ->
    all["assignments"].orEmpty().map { row ->
      val a=decode<HomeworkAssignment>(row)
      val done=all["assignment_submissions"].orEmpty().any { it.assignment_id==row.id && it.student_id==user.value.associatedStudentId && it.completed }
      a.copy(isCompletedByStudent=done,submissionStatus=if(done) "Submitted" else "Pending")
    }
  }
  override suspend fun createAssignment(assignment:HomeworkAssignment)=save("assignments",assignment.id,assignment,section(assignment.classGrade,assignment.division))
  override suspend fun toggleAssignmentCompletion(assignmentId:String) {
    val a=table("assignments").first { it.id==assignmentId };val st=user.value.associatedStudentId
    check(user.value.role==UserRole.STUDENT && st.isNotBlank()) { "Only the linked student can submit work." }
    val old=table("assignment_submissions").firstOrNull { it.assignment_id==assignmentId && it.student_id==st }
    write("assignment_submissions",JsonObject(boundary("${assignmentId}_$st",a.section_id,st)+mapOf("assignment_id" to JsonPrimitive(assignmentId),"completed" to JsonPrimitive(old?.completed!=true))))
  }
  override fun getStudyMaterialsFlow()=models<StudyMaterial>("study_materials")
  override suspend fun uploadStudyMaterial(material:StudyMaterial) {
    val sections=table("sections").filter { sec -> table("classes").any { it.id==sec.class_id && it.grade==material.classGrade } }
    check(sections.size==1) { "Choose an unambiguous class before sharing material." }
    save("study_materials",material.id,material,sections.single().id)
  }
  override fun getReportCardsFlow():Flow<List<ReportCard>> = rows.map { all ->
    all["marks"].orEmpty().groupBy { "${it.student_id}_${it.term}" }.mapNotNull { (_,scores) ->
      val first=scores.first();val s=all["students"].orEmpty().firstOrNull { it.id==first.student_id }?.let { decode<Student>(it) } ?: return@mapNotNull null
      ReportCard("${s.id}_${first.term}",s.id,s.name,s.classGrade,s.division,s.rollNumber,first.term,activeSession,
        scores.map { SubjectScore(it.subject,it.maximum,it.obtained,SubjectScore.calculateCbseGrade(it.obtained,it.maximum),"",it.payload["checkedPaperUrl"]?.jsonPrimitive?.content.orEmpty(),it.payload["answerKeyUrl"]?.jsonPrimitive?.content.orEmpty()) },
        s.attendancePercent,0,0,"","",s.schoolId,activeSession,scores.all { it.published })
    }
  }
  override suspend fun saveStudentMarks(studentId:String,term:String,subjectName:String,obtainedMarks:Int,maxMarks:Int) {
    requireSession()
    client.postgrest.rpc("save_draft_mark", buildJsonObject {
      put("p_student",studentId); put("p_term",term.trim()); put("p_subject",subjectName.trim())
      put("p_obtained",obtainedMarks); put("p_maximum",maxMarks)
    })
    refresh("marks")
  }
  override suspend fun saveCheckedPaper(studentId:String,term:String,subjectName:String,checkedPaperUrl:String) {
    updateMarkAttachments(table("marks").filter { it.student_id==studentId && it.term==term && it.subject==subjectName },"checkedPaperUrl",checkedPaperUrl)
  }
  override suspend fun saveAnswerKey(classGrade:String,term:String,subjectName:String,answerKeyUrl:String) {
    val ids=table("students").filter { it.payload["classGrade"]?.jsonPrimitive?.content==classGrade }.map { it.id }
    updateMarkAttachments(table("marks").filter { it.student_id in ids && it.term==term && it.subject==subjectName },"answerKeyUrl",answerKeyUrl)
  }
  private suspend fun updateMarkAttachments(marks:List<SupabaseRow>,key:String,url:String) {
    check(marks.isNotEmpty()) { "Save marks before attaching documents." }
    marks.forEach { m -> write("marks",JsonObject(boundary(m.id,m.section_id,m.student_id)+mapOf("term" to JsonPrimitive(m.term),"subject" to JsonPrimitive(m.subject),"obtained" to JsonPrimitive(m.obtained),"maximum" to JsonPrimitive(m.maximum),"published" to JsonPrimitive(m.published),"payload" to JsonObject(m.payload+(key to JsonPrimitive(url)))))) }
  }
  override suspend fun publishReportCard(studentId:String,term:String,published:Boolean) {
    requireSession()
    client.postgrest.rpc("set_result_publication", buildJsonObject {
      put("p_student",studentId); put("p_term",term); put("p_published",published)
    })
    refresh("marks")
  }
  override fun getExamSchedulesFlow()=models<ExamSchedule>("exams")
  override fun getTimetableFlow()=models<TimetableSlot>("timetables")
  override fun getEventsFlow()=models<SchoolEvent>("events")
  override fun getNoticesFlow()=models<SchoolNotice>("notices")
  override suspend fun publishNotice(notice:SchoolNotice)=save("notices",notice.id,notice)
  override fun getNotificationsFlow():Flow<List<ErpNotification>> = rows.map { all ->
    all["notifications"].orEmpty().sortedByDescending { it.created_at }.map { n ->
      ErpNotification(n.id,n.title,n.message,runCatching { NotificationCategory.valueOf(n.category) }.getOrDefault(NotificationCategory.SYSTEM),n.created_at,n.is_read,schoolId=n.school_id)
    }
  }
  override suspend fun addNotification(title:String,message:String,category:NotificationCategory,targetRole:UserRole?):Unit = error("The database creates notifications from saved school events.")
  override suspend fun markNotificationAsRead(id:String) {
    requireSession()
    val revision = sessionRevision.get()
    check(table("notifications").any { it.id==id }) { "This update is no longer available on your account." }
    client.from("notifications").update(buildJsonObject { put("is_read",true) }) { filter { eq("id",id) } }
    refresh("notifications")
    check(sessionRevision.get()==revision && table("notifications").any { it.id==id && it.is_read }) {
      "Read status could not be confirmed. Refresh the inbox and retry."
    }
  }
  override suspend fun markAllNotificationsAsRead() {
    requireSession()
    val revision = sessionRevision.get(); val recipient = user.value.id
    client.from("notifications").update(buildJsonObject { put("is_read",true) }) { filter { eq("recipient_id",recipient) } }
    refresh("notifications")
    check(sessionRevision.get()==revision && table("notifications").all { it.is_read }) { "Inbox read state was not confirmed." }
  }
  override fun getLeaveRequestsFlow()=models<LeaveRequest>("leave_requests")
  override suspend fun applyLeave(applicantName:String,applicantRole:UserRole,applicantId:String,classOrDept:String,leaveType:String,startDate:String,endDate:String,daysCount:Int,reason:String) {
    val id=UUID.randomUUID().toString()
    save("leave_requests",id,LeaveRequest(id,user.value.id,user.value.name,user.value.role,classOrDept,leaveType,startDate,endDate,daysCount,reason,"Today",LeaveStatus.PENDING,schoolId=user.value.schoolId))
  }
  override suspend fun updateLeaveStatus(leaveId:String,status:LeaveStatus,approverRemark:String) {
    requireSession()
    client.postgrest.rpc("decide_leave",buildJsonObject { put("p_id",leaveId);put("p_status",status.name);put("p_remark",approverRemark) })
    refresh("leave_requests")
  }
  override fun getTransportRoutesFlow()=models<TransportRoute>("transport_routes")
  override fun getLibraryBooksFlow()=models<LibraryBook>("library_books")
  override suspend fun issueBook(bookId:String):Unit=error("Library circulation is not enabled in the Supabase demo yet.")
  override suspend fun returnBook(bookId:String):Unit=error("Library circulation is not enabled in the Supabase demo yet.")
  override fun getInventoryFlow()=models<InventoryAsset>("inventory")
  fun auditFlow():Flow<List<AuditLogEntry>> = rows.map { all -> all["audit_logs"].orEmpty().sortedByDescending { it.created_at }.map { AuditLogEntry(it.id,"School account","",it.action,"${it.table_name}: ${it.action}",it.created_at,it.record_id) } }
  fun gradebookFlow():Flow<List<GradebookEntry>> = rows.map { all ->
    all["marks"].orEmpty().mapNotNull { mark ->
      val student=all["students"].orEmpty().firstOrNull { it.id==mark.student_id }?.let { decode<Student>(it) } ?: return@mapNotNull null
      GradebookEntry(mark.id,student.id,student.name,student.fullClass,mark.term,mark.subject,mark.obtained,mark.maximum,
        SubjectScore.calculateCbseGrade(mark.obtained,mark.maximum),0.0,date="",instructorName="School faculty")
    }
  }
  fun sessionId():String { requireSession();return activeSession }
  private fun signedOutUser()=UserProfile("","","",UserRole.STUDENT,"","","",schoolId="")
}
