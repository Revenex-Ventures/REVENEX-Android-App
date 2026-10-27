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
  val attachment_ref:String="",
  val attachment_name:String="",
  val note:String="",
  val feedback:String="",
  val review_status:String="SUBMITTED",
  val version:Int=0,
  val submitted_at:String?=null,
  val reviewed_at:String?=null,
  val actionable_id:String="",
  val publication_state:String="PUBLISHED",
  val closes_at:String?=null,
  val review_state:String="DRAFT",
  val action: String = "",
  val table_name: String = "",
  val record_id: String = ""
)

data class BackendSyncState(val busy: Boolean = false, val message: String = "", val failed: Boolean = false)

/** Derive summaries only from rows already authorized by PostgreSQL RLS. */
internal fun summarizeSupabaseStudent(student: Student, fees: List<FeeRecord>, attendance: List<SupabaseRow>): Student {
  val permittedFees=fees.filter { it.studentId==student.id }
  val knownStatuses=AttendanceStatus.entries.map { it.name }.toSet()
  val attendedStatuses=setOf("PRESENT","LATE","EXCUSED")
  val records=attendance.filter { it.student_id==student.id && it.status in knownStatuses }
  return student.copy(
    feeSummaryAvailable=permittedFees.isNotEmpty(),
    feePendingAmount=permittedFees.sumOf { it.pendingAmount },
    feeStatus=permittedFees.firstOrNull { it.status==FeeStatus.OVERDUE }?.status
      ?: permittedFees.firstOrNull { it.pendingAmount>0 }?.status ?: FeeStatus.PAID,
    attendancePercent=if(records.isEmpty()) 0.0 else 100.0*records.count { it.status in attendedStatuses }/records.size,
    rank=0,gpa=0.0
  )
}

/** Submission status is child-specific, even when a guardian can read several children. */
internal fun assignmentForStudent(assignment: HomeworkAssignment, submissions: List<SupabaseRow>, studentId: String): HomeworkAssignment {
  val done=studentId.isNotBlank() && submissions.any {it.assignment_id==assignment.id && it.student_id==studentId && it.completed}
  return assignment.copy(isCompletedByStudent=done,submissionStatus=if(done)"Submitted" else "Pending")
}

/** No sample fallback. PostgreSQL RLS, not these filters, is the authorization boundary. */
class SupabaseDataSource : ErpDataSource {
  private val client get() = SupabaseProvider.client
  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val rows = MutableStateFlow<Map<String,List<SupabaseRow>>>(emptyMap())
  private val user = MutableStateFlow(signedOutUser())
  private val presentationStudent = MutableStateFlow("")
  val syncState = MutableStateFlow(BackendSyncState())
  private val capabilities=MutableStateFlow(SchoolBackendCapabilities())
  override fun getBackendCapabilitiesFlow():Flow<SchoolBackendCapabilities> = capabilities
  private suspend fun loadCapabilities():SchoolBackendCapabilities = try {
    client.postgrest.rpc("erp_capabilities").decodeAs<SchoolBackendCapabilities>()
  }catch(e:CancellationException){throw e}catch(_:Exception){SchoolBackendCapabilities()}
  private var liveJob: Job? = null
  private var authenticated = false
  private val lifecycleLock=Mutex()
  private val reconciliationLock=Mutex()
  private val sessionRevision=AtomicLong()
  private val tables = listOf("students","teachers","classes","sections","timetables","notices","attendance",
    "assignments","assignment_submissions","marks","exams","fee_records","study_materials","leave_requests",
    "events","transport_routes","library_books","inventory","notifications","audit_logs")
  private val snapshotTables=tables+listOf("section_teachers","parent_student_links","subjects")

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
  /** Atomic replacement drops revoked rows and catches missed submission/allocation events. */
  suspend fun reconcileSchoolData() = reconciliationLock.withLock {
    requireSession()
    val revision=sessionRevision.get();val actor=user.value;val session=activeSession
    try {
      val snapshot=fetchSchoolSnapshot(actor,session)
      val available=loadCapabilities()
      rows.update { old -> if(authenticated && sessionRevision.get()==revision) snapshot else old }
      if(authenticated && sessionRevision.get()==revision)capabilities.value=available
      if(sessionRevision.get()==revision && !syncState.value.busy)syncState.value=BackendSyncState(message="School data reconciled")
    } catch(e:Exception) {
      if(e is CancellationException)throw e
      if(sessionRevision.get()==revision && e is SchoolAccessChanged){
        sessionRevision.incrementAndGet();authenticated=false;rows.value=emptyMap();presentationStudent.value=""
        user.value=signedOutUser();activeSession="";capabilities.value=SchoolBackendCapabilities()
        syncState.value=BackendSyncState(message=e.message.orEmpty(),failed=true)
        liveJob?.cancel()
        withContext(NonCancellable){runCatching{withTimeout(5000){client.realtime.removeAllChannels();client.auth.clearSession()}}}
      } else if(sessionRevision.get()==revision){
        syncState.value=BackendSyncState(message="Unable to reconcile school data. Displayed data may be stale; retry when connected.",failed=true)
      }
      throw e
    }
  }
  private suspend fun fetchSchoolSnapshot(actor:UserProfile,session:String,beforeRead:(String)->Unit={}):Map<String,List<SupabaseRow>> {
    suspend fun profile()=client.from("profiles").select{filter{eq("id",actor.id)}}.decodeList<SupabaseRow>().singleOrNull()
    validateReconciledScope(actor,session,profile(),emptyMap())
    val snapshot=snapshotTables.associateWith { beforeRead(it);client.from(it).select().decodeList<SupabaseRow>() }
    validateReconciledScope(actor,session,profile(),snapshot)
    return snapshot
  }
  suspend fun authenticate(email: String, password: String) = lifecycleLock.withLock {
    authenticateSession(email,password)
  }
  @OptIn(SupabaseExperimental::class)
  private suspend fun authenticateSession(email: String, password: String) {
    SupabaseProvider.configuration.validate()
    liveJob?.cancelAndJoin()
    sessionRevision.incrementAndGet()
    rows.value=emptyMap();presentationStudent.value=""; authenticated=false;user.value=signedOutUser();activeSession="";capabilities.value=SchoolBackendCapabilities()
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
      // Preserve the backend ADMIN identity, but never load or route its mobile portal.
      MobileRoleAccess.requireSupported(role)
      user.value=UserProfile(uid,profile.name,profile.email,role,role.displayName,"",profile.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),schoolId=profile.school_id)
      activeSession=profile.session_id
      signInStage="school data"
      rows.value=fetchSchoolSnapshot(user.value,activeSession){signInStage=it}
      capabilities.value=loadCapabilities()
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
              reconcileSchoolData()
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
                // Realtime's local cache can retain rows after scope changes. Re-read through RLS.
                refresh(name)
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
      liveJob?.cancel()
      liveJob = null
      authenticated = false
      rows.value = emptyMap()
      capabilities.value=SchoolBackendCapabilities()
      user.value = signedOutUser()
      activeSession = ""
      withContext(NonCancellable){runCatching{withTimeout(5000){client.realtime.removeAllChannels();client.auth.clearSession()}}}
      if(e is CancellationException){syncState.value=BackendSyncState();throw e}
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
  private suspend fun <T> confirmedChange(persist: suspend () -> T, reload: suspend () -> Unit): T =
    confirmInSession({sessionRevision.get()},{authenticated},persist,reload)
  private suspend fun write(table: String, body: JsonObject) {
    requireSession();val revision=sessionRevision.get()
    syncState.value=BackendSyncState(true,"Saving to school database…")
    try {
      confirmedChange({client.from(table).upsert(body)},{refresh(table)})
      syncState.value=BackendSyncState(message="Saved to school database")
    } catch(e:Exception) {
      if(e is CancellationException) throw e
      if(sessionRevision.get()!=revision) throw IllegalStateException("Account changed. Check the original account before retrying.")
      syncState.value=BackendSyncState(message="Save was not confirmed. Check connection and access before retrying.",failed=true)
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
    liveJob?.cancelAndJoin();liveJob=null;authenticated=false;rows.value=emptyMap();presentationStudent.value="";user.value=signedOutUser();activeSession=""
    syncState.value=BackendSyncState();capabilities.value=SchoolBackendCapabilities()
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
  override fun getTeachersFlow():Flow<List<Teacher>> = rows.map { all ->
    all["teachers"].orEmpty().map { row -> decode<Teacher>(row).copy(assignedClasses=authoritativeTeacherClasses(row.profile_id.orEmpty(),all)) }
  }
  override suspend fun addStudent(student:Student) { enrollStudent(student,0L) }
  suspend fun enrollStudent(student:Student,feeAmount:Long) {
    requireSession()
    require(user.value.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) { "Only the principal or school administrator can enroll students." }
    require(feeAmount in 0L..1000000000L) { "Invalid fee amount." }
    val revision=sessionRevision.get()
    syncState.value=BackendSyncState(true,"Saving enrollment to Supabase…")
    try {
      confirmedChange({ client.postgrest.rpc("enroll_student",buildJsonObject {
        put("p_id",student.id);put("p_section",section(student.classGrade,student.division))
        put("p_payload",payload(student));put("p_fee_amount",feeAmount)
      }) }, { listOf("students","fee_records","notifications","audit_logs").forEach { refresh(it) } })
      syncState.value=BackendSyncState(message="Student enrolled in school database. School inbox updated.")
    } catch(e:Exception) {
      if(e is CancellationException) throw e
      if(sessionRevision.get()!=revision) throw IllegalStateException("Account changed. Check the original account before retrying.")
      syncState.value=BackendSyncState(message="Enrollment was not confirmed. Check the student directory before retrying. Contact your school administrator if the record is missing.",failed=true)
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
          AttendanceStatus.valueOf(record.status),record.payload["remark"]?.jsonPrimitive?.content.orEmpty(),record.version)
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
    check(capabilities.value.loaded&&capabilities.value.attendance){"School attendance backend update required. Your register was not submitted."}
    require(studentList.isNotEmpty()) { "Select a populated class before saving attendance." }
    val day=parseSchoolDate(date)?:error("Attendance date is invalid.")
    val batch=buildJsonArray { studentList.forEach { st -> add(buildJsonObject {
      put("student_id",st.studentId);put("status",st.status.name);put("remark",st.remark);put("expected_version",st.revision)
    }) } }
    syncState.value=BackendSyncState(true,"Saving attendance…")
    val revision=sessionRevision.get()
    try { confirmedChange({client.postgrest.rpc("save_roll_call",buildJsonObject{
      put("p_section",sec);put("p_date",day.toString());put("p_slot",slot);put("p_entries",batch)
    })},{refresh("attendance")});syncState.value=BackendSyncState(message="Attendance saved") }
    catch(e:Exception) { if(e is CancellationException) throw e;if(sessionRevision.get()!=revision) throw IllegalStateException("Account changed. Check the original account before retrying.");syncState.value=BackendSyncState(message="Attendance was not saved. Check access and network.",failed=true);throw IllegalStateException(syncState.value.message) }
  }
  fun selectStudentForPresentation(id: String) {
    requireSession()
    check(user.value.role==UserRole.PARENT && table("students").any {it.id==id}) {"Student is not available to this account."}
    presentationStudent.value=id
  }
  override fun getAssignmentsFlow():Flow<List<HomeworkAssignment>> = combine(rows,presentationStudent,user) {all,selected,actor ->
    val child=if(actor.role==UserRole.PARENT && all["students"].orEmpty().any {it.id==selected}) selected else actor.associatedStudentId
    all["assignments"].orEmpty().map { row -> assignmentForStudent(decode<HomeworkAssignment>(row).copy(publicationState=row.publication_state,revision=row.version,closesAt=row.closes_at.orEmpty()),all["assignment_submissions"].orEmpty(),child) }
  }
  override suspend fun createAssignment(assignment:HomeworkAssignment) {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.assignments){"School assignment backend update required. Refresh after setup."}
    val due=listOf(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE,java.time.format.DateTimeFormatter.ofPattern("dd MMM uuuu",java.util.Locale.US))
      .firstNotNullOfOrNull{runCatching{java.time.LocalDate.parse(assignment.dueDate,it)}.getOrNull()}
      ?:error("Use a valid due date, for example 28 Oct 2026.")
    confirmedChange({client.postgrest.rpc("write_assignment",buildJsonObject{
      put("p_id",assignment.id);put("p_section",section(assignment.classGrade,assignment.division));put("p_payload",payload(assignment))
      put("p_due_date",due.toString());put("p_publish",assignment.publicationState=="PUBLISHED");put("p_expected_version",assignment.revision)
    })},{listOf("assignments","notifications").forEach{refresh(it)}})
  }
  override suspend fun toggleAssignmentCompletion(assignmentId:String) {
    error("Open the assignment and submit a file or note. Cloud submissions cannot be changed by a completion toggle.")
  }
  private fun submission(row:SupabaseRow,all:Map<String,List<SupabaseRow>> = rows.value)=AssignmentSubmission(
    row.id,row.assignment_id,row.student_id.orEmpty(),all["students"].orEmpty().firstOrNull{it.id==row.student_id}?.payload?.get("name")?.jsonPrimitive?.content.orEmpty(),
    row.attachment_ref,row.attachment_name,row.note,row.feedback,row.review_status,row.version,row.submitted_at.orEmpty(),row.reviewed_at.orEmpty(),row.school_id,row.session_id)
  override fun getAssignmentSubmissionsFlow():Flow<List<AssignmentSubmission>> = rows.map{all->
    all["assignment_submissions"].orEmpty().filter{it.completed&&it.version>0}.map{submission(it,all)}
  }
  override suspend fun submitAssignment(assignmentId:String,attachmentRef:String,note:String,expectedVersion:Int):AssignmentSubmission {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.submissions){"School submission backend update required. Your draft is retained."}
    check(user.value.role==UserRole.STUDENT){"Only the linked student can submit work."}
    require(note.length<=8000 && (note.isNotBlank() || attachmentRef.isNotBlank()) && expectedVersion>=0)
    val saved=confirmedChange({client.postgrest.rpc("submit_assignment",buildJsonObject{
      put("p_assignment",assignmentId);put("p_attachment",attachmentRef);put("p_note",note);put("p_expected_version",expectedVersion)
    }).decodeAs<SupabaseRow>()},{listOf("assignment_submissions","notifications").forEach{refresh(it)}})
    return submission(saved)
  }
  override suspend fun reviewSubmission(id:String,feedback:String,expectedVersion:Int):AssignmentSubmission {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.submissions){"School submission backend update required."}
    check(user.value.role in setOf(UserRole.TEACHER,UserRole.PRINCIPAL,UserRole.ADMIN)){"Staff review access required."}
    require(feedback.trim().length in 1..4000 && expectedVersion>=0)
    val saved=confirmedChange({client.postgrest.rpc("review_submission",buildJsonObject{
      put("p_submission",id);put("p_feedback",feedback);put("p_expected_version",expectedVersion)
    }).decodeAs<SupabaseRow>()},{listOf("assignment_submissions","notifications").forEach{refresh(it)}})
    return submission(saved)
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
        scores.map { SubjectScore(it.subject,it.maximum,it.obtained,SubjectScore.calculateCbseGrade(it.obtained,it.maximum),"",it.payload["checkedPaperUrl"]?.jsonPrimitive?.content.orEmpty(),it.payload["answerKeyUrl"]?.jsonPrimitive?.content.orEmpty(),it.version,it.review_state) },
        s.attendancePercent,0,0,"","",s.schoolId,activeSession,scores.all { it.published })
    }
  }
  override suspend fun saveStudentMarks(studentId:String,term:String,subjectName:String,obtainedMarks:Int,maxMarks:Int) {
    error("Reload the marks editor before saving. Blind cloud mark updates are disabled.")
  }
  override suspend fun saveDraftMarks(studentId:String,term:String,subject:String,obtained:Int,maximum:Int,expectedVersion:Int):Int {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.marks){"School marks backend update required. Your draft is retained."}
    return confirmedChange({ client.postgrest.rpc("save_draft_mark", buildJsonObject {
      put("p_student",studentId); put("p_term",term.trim()); put("p_subject",subject.trim())
      put("p_obtained",obtained); put("p_maximum",maximum);put("p_expected_version",expectedVersion)
    }).decodeAs<SupabaseRow>() }, { refresh("marks") }).version
  }
  override suspend fun submitMarksForReview(studentId:String,term:String,subject:String,expectedVersion:Int):Int {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.marks){"School marks backend update required."}
    return confirmedChange({client.postgrest.rpc("submit_mark_review",buildJsonObject{
      put("p_student",studentId);put("p_term",term);put("p_subject",subject);put("p_expected_version",expectedVersion)
    }).decodeAs<SupabaseRow>()},{listOf("marks","notifications").forEach{refresh(it)}}).version
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
    marks.forEach { m -> confirmedChange({client.postgrest.rpc("attach_mark_document",buildJsonObject{
      put("p_id",m.id);put("p_field",key);put("p_ref",url);put("p_expected_version",m.version)
    })},{refresh("marks")}) }
  }
  override suspend fun publishReportCard(studentId:String,term:String,published:Boolean) {
    requireSession()
    check(user.value.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) { "Result publication requires school approval." }
    confirmedChange({ client.postgrest.rpc("set_result_publication", buildJsonObject {
      put("p_student",studentId); put("p_term",term); put("p_published",published)
    }) }, { refresh("marks") })
  }
  override fun getExamSchedulesFlow()=models<ExamSchedule>("exams")
  override fun getTimetableFlow()=models<TimetableSlot>("timetables")
  override fun getEventsFlow()=models<SchoolEvent>("events")
  override fun getNoticesFlow():Flow<List<SchoolNotice>> = rows.map{all->all["notices"].orEmpty().map{decode<SchoolNotice>(it).copy(revision=it.version)}}
  override suspend fun publishNotice(notice:SchoolNotice) {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.targeted_notices){"School notice backend update required. Your notice was not submitted."}
    val target=if(notice.targetAudience=="Class")section(notice.targetClass.substringBeforeLast('-'),notice.targetClass.substringAfterLast('-'))else null
    confirmedChange({client.postgrest.rpc("write_notice",buildJsonObject{
      put("p_id",notice.id);put("p_payload",payload(notice));put("p_audience",notice.targetAudience)
      put("p_section",target?.let{JsonPrimitive(it)}?:JsonNull);put("p_expected_version",notice.revision)
    })},{listOf("notices","notifications").forEach{refresh(it)}})
  }
  override fun getNotificationsFlow():Flow<List<ErpNotification>> = rows.map { all ->
    all["notifications"].orEmpty().sortedByDescending { it.created_at }.map { n ->
      ErpNotification(n.id,n.title,n.message,runCatching { NotificationCategory.valueOf(n.category) }.getOrDefault(NotificationCategory.SYSTEM),n.created_at,n.is_read,actionableId=n.actionable_id,schoolId=n.school_id)
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
  override suspend fun requestLeave(id:String,type:String,start:String,end:String,reason:String) {
    requireSession()
    check(capabilities.value.loaded&&capabilities.value.leaves){"School leave backend update required. Your request was not submitted."}
    val first=parseSchoolDate(start)?:error("Enter a valid start date.")
    val last=parseSchoolDate(end)?:error("Enter a valid end date.")
    confirmedChange({client.postgrest.rpc("request_leave",buildJsonObject{
      put("p_id",id);put("p_type",type);put("p_start",first.toString());put("p_end",last.toString());put("p_reason",reason)
    })},{listOf("leave_requests","notifications").forEach{refresh(it)}})
  }
  override suspend fun applyLeave(applicantName:String,applicantRole:UserRole,applicantId:String,classOrDept:String,leaveType:String,startDate:String,endDate:String,daysCount:Int,reason:String) {
    error("Use the confirmed leave form so retries retain the original request reference.")
  }
  override suspend fun updateLeaveStatus(leaveId:String,status:LeaveStatus,approverRemark:String) {
    requireSession()
    confirmedChange({client.postgrest.rpc("decide_leave",buildJsonObject { put("p_id",leaveId);put("p_status",status.name);put("p_remark",approverRemark) })},{refresh("leave_requests")})
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
