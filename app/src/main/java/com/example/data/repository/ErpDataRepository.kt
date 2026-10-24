package com.example.data.repository

import android.util.Log
import com.example.data.datasource.AppEnvironment
import com.example.data.datasource.DataMode
import com.example.data.datasource.ErpDataSource
import com.example.data.datasource.DocumentFiles
import com.example.data.datasource.LocalDemoDataSource
import com.example.data.datasource.SupabaseDataSource
import com.example.data.datasource.BackendSyncState
import com.example.data.datasource.SupabaseFileStore
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ErpDataRepository — Singleton state holder and coordinator for the entire ERP.
 *
 * Architecture: Repository exposes StateFlows that are populated by the active DataSource
 * via bindDataSource(). All mutation operations delegate to the active DataSource, and
 * the DataSource's internal StateFlow updates automatically propagate to the Repository
 * via the collectors in bindDataSource().
 *
 * This eliminates the triple-write problem where both Repository and DataSource would
 * independently update the same data, causing race conditions.
 */
class ErpDataRepository private constructor() {

  private val tag = "ErpDataRepository"
  private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, error ->
    _backendState.value = BackendSyncState(message = error.message?.takeIf { it.length < 180 } ?: "School operation failed. Please retry.", failed = true)
  })

  val demoDataSource = LocalDemoDataSource()
  val supabaseDataSource by lazy { SupabaseDataSource() }
  private val _backendState = MutableStateFlow(BackendSyncState())
  val backendState: StateFlow<BackendSyncState> = _backendState.asStateFlow()

  // Current Mode
  private val _dataMode = MutableStateFlow(AppEnvironment.currentMode)
  val dataMode: StateFlow<DataMode> = _dataMode.asStateFlow()

  // All state is driven by the active DataSource via bindDataSource() collectors.
  // Repository does NOT directly mutate these — DataSource mutations propagate automatically.
  private val _currentUser = MutableStateFlow(SampleData.defaultProfiles[0])
  val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

  private val _students = MutableStateFlow(SampleData.initialStudents)
  val students: StateFlow<List<Student>> = _students.asStateFlow()

  private val _teachers = MutableStateFlow(SampleData.initialTeachers)
  val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

  private val _feeRecords = MutableStateFlow(SampleData.initialFeeRecords)
  val feeRecords: StateFlow<List<FeeRecord>> = _feeRecords.asStateFlow()

  private val _assignments = MutableStateFlow(SampleData.initialAssignments)
  val assignments: StateFlow<List<HomeworkAssignment>> = _assignments.asStateFlow()

  private val _studyMaterials = MutableStateFlow(SampleData.initialStudyMaterials)
  val studyMaterials: StateFlow<List<StudyMaterial>> = _studyMaterials.asStateFlow()

  private val _notices = MutableStateFlow(SampleData.initialNotices)
  val notices: StateFlow<List<SchoolNotice>> = _notices.asStateFlow()

  private val _leaveRequests = MutableStateFlow(SampleData.initialLeaveRequests)
  val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

  private val _reportCards = MutableStateFlow(SampleData.initialReportCards)
  val reportCards: StateFlow<List<ReportCard>> = _reportCards.asStateFlow()

  private val _examSchedules = MutableStateFlow(SampleData.initialExamSchedules)
  val examSchedules: StateFlow<List<ExamSchedule>> = _examSchedules.asStateFlow()

  private val _timetable = MutableStateFlow(SampleData.initialTimetable)
  val timetable: StateFlow<List<TimetableSlot>> = _timetable.asStateFlow()

  private val _events = MutableStateFlow(SampleData.initialEvents)
  val events: StateFlow<List<SchoolEvent>> = _events.asStateFlow()

  private val _transportRoutes = MutableStateFlow(SampleData.initialTransportRoutes)
  val transportRoutes: StateFlow<List<TransportRoute>> = _transportRoutes.asStateFlow()

  private val _libraryBooks = MutableStateFlow(SampleData.initialLibraryBooks)
  val libraryBooks: StateFlow<List<LibraryBook>> = _libraryBooks.asStateFlow()

  private val _inventory = MutableStateFlow(SampleData.initialInventory)
  val inventory: StateFlow<List<InventoryAsset>> = _inventory.asStateFlow()

  private val _notifications = MutableStateFlow(SampleData.initialNotifications)
  val notifications: StateFlow<List<ErpNotification>> = _notifications.asStateFlow()

  private val _auditLogs = MutableStateFlow(SampleData.initialAuditLogs)
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  private val _invoices = MutableStateFlow(SampleData.initialInvoices)
  val invoices: StateFlow<List<LedgerInvoice>> = _invoices.asStateFlow()

  private val _gradebookEntries = MutableStateFlow(SampleData.initialGradebookEntries)
  val gradebookEntries: StateFlow<List<GradebookEntry>> = _gradebookEntries.asStateFlow()

  fun addAuditLog(entry: AuditLogEntry) {
    if (_dataMode.value == DataMode.SUPABASE_DEMO) return
    _auditLogs.value = listOf(entry) + _auditLogs.value
  }

  fun processInvoicePayment(invoiceId: String, amountPaid: Long, channel: String = "Stripe ACH") {
    if (_dataMode.value != DataMode.DEMO) {
      _backendState.value=BackendSyncState(message="Payments are disabled. Fee balances are read-only.",failed=true)
      return
    }
    _invoices.value = _invoices.value.map { inv ->
      if (inv.id == invoiceId) {
        val newPaid = (inv.paidAmount + amountPaid).coerceAtMost(inv.totalAmount)
        val newStatus = if (newPaid >= inv.totalAmount) FeeStatus.PAID else FeeStatus.PARTIAL
        inv.copy(paidAmount = newPaid, status = newStatus, paymentChannel = channel)
      } else inv
    }
    addAuditLog(
      AuditLogEntry(
        id = "aud_${System.currentTimeMillis()}",
        actorName = _currentUser.value.name,
        actorRole = _currentUser.value.role.displayName,
        actionType = "FEE_PAYMENT",
        actionSummary = "Tuition Payment Processed",
        timestamp = "Just now",
        details = "Collected $${amountPaid / 100L} via $channel for Invoice #$invoiceId.",
        targetScholar = "Invoice #$invoiceId",
        iconType = "payment"
      )
    )
  }

  fun recordRollCallSession(
    classGrade: String,
    division: String,
    sessionSlot: String,
    studentList: List<StudentAttendance>,
    onResult: ((Boolean) -> Unit)? = null
  ) {
    if (_dataMode.value == DataMode.SUPABASE_DEMO) {
      repositoryScope.launch {
        try {
          supabaseDataSource.saveRollCall(classGrade,division,getCurrentFormattedDate(),studentList,sessionSlot)
          onResult?.invoke(true)
        } catch(e:Exception) {
          if(e is CancellationException) throw e
          _backendState.value=BackendSyncState(message="Attendance was not saved. Check access and connection.",failed=true)
          onResult?.invoke(false)
        }
      }
      return
    }
    val present = studentList.count { it.status == AttendanceStatus.PRESENT }
    val absent = studentList.count { it.status == AttendanceStatus.ABSENT }
    val late = studentList.count { it.status == AttendanceStatus.LATE }
    val excused = studentList.count { it.status == AttendanceStatus.EXCUSED }

    val rec = ClassAttendanceRecord(
      id = "att_${classGrade}${division}_${System.currentTimeMillis()}",
      classGrade = classGrade,
      division = division,
      sessionSlot = sessionSlot,
      date = getCurrentFormattedDate(),
      totalStudents = studentList.size,
      presentCount = present,
      absentCount = absent,
      lateCount = late,
      excusedCount = excused,
      markedBy = _currentUser.value.name,
      studentList = studentList
    )

    val currentMap = _classAttendance.value.toMutableMap()
    currentMap["${classGrade}-${division}_${sessionSlot}"] = rec
    _classAttendance.value = currentMap

    addAuditLog(
      AuditLogEntry(
        id = "aud_${System.currentTimeMillis()}",
        actorName = _currentUser.value.name,
        actorRole = _currentUser.value.role.displayName,
        actionType = "ROLL_CALL",
        actionSummary = "$sessionSlot Verified",
        timestamp = "Just now",
        details = "Class $classGrade-$division $sessionSlot submitted. $present Present, $late Late, $absent Absent, $excused Excused.",
        targetScholar = "Class $classGrade-$division",
        iconType = "attendance"
      )
    )
    onResult?.invoke(true)
  }

  private val _classAttendance = MutableStateFlow<Map<String, ClassAttendanceRecord>>(
    mapOf(
      "10-A_today" to ClassAttendanceRecord(
        id = "att_10A_today",
        classGrade = "10",
        division = "A",
        date = getCurrentFormattedDate(),
        totalStudents = 6,
        presentCount = 5,
        absentCount = 1,
        lateCount = 0,
        leaveCount = 0,
        markedBy = "Prof. Sunita Rao",
        studentList = listOf(
          StudentAttendance("stu_1001", "Aarav Patel", 14, AttendanceStatus.PRESENT),
          StudentAttendance("stu_1002", "Ananya Sharma", 15, AttendanceStatus.PRESENT),
          StudentAttendance("stu_1003", "Rohan Gupta", 22, AttendanceStatus.PRESENT),
          StudentAttendance("stu_1004", "Priya Verma", 28, AttendanceStatus.PRESENT),
          StudentAttendance("stu_1005", "Vikram Reddy", 31, AttendanceStatus.ABSENT, "Medical sick leave"),
          StudentAttendance("stu_1006", "Kavya Deshpande", 34, AttendanceStatus.PRESENT)
        )
      )
    )
  )
  val classAttendance: StateFlow<Map<String, ClassAttendanceRecord>> = _classAttendance.asStateFlow()

  private val _selectedStudentId = MutableStateFlow<String>("")
  val selectedStudentId: StateFlow<String> = _selectedStudentId.asStateFlow()

  fun setSelectedStudentId(id: String) {
    if(_dataMode.value==DataMode.SUPABASE_DEMO) supabaseDataSource.selectStudentForPresentation(id)
    _selectedStudentId.value = id
  }

  private val _isAppLockEnabled = MutableStateFlow(false)
  val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

  fun setAppLockEnabled(enabled: Boolean) {
    _isAppLockEnabled.value = enabled
  }

  private var lastUserId: String? = null

  private fun updateSelectedStudent() {
    val user = _currentUser.value
    if (user.id != lastUserId) {
      lastUserId = user.id
      _selectedStudentId.value = ""
    }
    if (user.id.isBlank()) {
      _selectedStudentId.value = ""
      return
    }
    if (user.role == UserRole.PARENT) {
      val wards = _students.value.filter { st ->
        user.associatedChildNames.contains(st.id) ||
        (user.phone.isNotBlank() && st.parentPhone.replace("+91", "").trim().replace(" ", "") == user.phone.replace("+91", "").trim().replace(" ", "")) ||
        st.id == user.associatedStudentId
      }
      if (wards.isNotEmpty() && (_selectedStudentId.value.isBlank() || wards.none { it.id == _selectedStudentId.value })) {
        _selectedStudentId.value = wards.first().id
      }
    } else if (user.role == UserRole.STUDENT) {
      _selectedStudentId.value = if (_dataMode.value == DataMode.SUPABASE_DEMO) {
        user.associatedStudentId
      } else {
        user.associatedStudentId.ifBlank { "stu_1001" }
      }
    }
  }

  private var activeSourceJob: Job? = null

  init {
    bindDataSource()
  }

  val activeSource: ErpDataSource
    get() = when (_dataMode.value) {
      DataMode.DEMO -> demoDataSource
      DataMode.SUPABASE_DEMO -> supabaseDataSource
    }

  /**
   * Binds all Repository StateFlows to the active DataSource's flows.
   * This is the SINGLE mechanism by which DataSource state propagates to the UI.
   * Repository mutation methods delegate to the DataSource, which updates its own flows,
   * and these collectors automatically sync the changes to the Repository's exposed StateFlows.
   */
  private fun bindDataSource() {
    activeSourceJob?.cancel()
    activeSourceJob = repositoryScope.launch {
      val source = activeSource
      if (source is SupabaseDataSource) {
        _students.value=emptyList();_teachers.value=emptyList();_feeRecords.value=emptyList()
        _assignments.value=emptyList();_studyMaterials.value=emptyList();_notices.value=emptyList()
        _leaveRequests.value=emptyList();_reportCards.value=emptyList();_examSchedules.value=emptyList()
        _timetable.value=emptyList();_events.value=emptyList();_transportRoutes.value=emptyList()
        _libraryBooks.value=emptyList();_inventory.value=emptyList();_notifications.value=emptyList()
        _classAttendance.value=emptyMap();_auditLogs.value=emptyList();_invoices.value=emptyList();_gradebookEntries.value=emptyList()
        _selectedStudentId.value=""
        launch { source.syncState.collect { _backendState.value=it } }
        launch { source.auditFlow().collect { _auditLogs.value=it } }
        launch { source.gradebookFlow().collect { _gradebookEntries.value=it } }
      } else {
        _backendState.value=BackendSyncState()
        _auditLogs.value=SampleData.initialAuditLogs;_invoices.value=SampleData.initialInvoices
        _gradebookEntries.value=SampleData.initialGradebookEntries
      }
      launch {
        source.getCurrentUserFlow().collect {
          _currentUser.value = it
          updateSelectedStudent()
        }
      }
      launch {
        source.getStudentsFlow().collect {
          _students.value = it
          updateSelectedStudent()
        }
      }
      launch { source.getTeachersFlow().collect { _teachers.value = it } }
      launch {
        source.getFeeRecordsFlow().collect { records ->
          _feeRecords.value = records
          if (source is SupabaseDataSource) {
            _invoices.value = records.map { fee ->
              LedgerInvoice(
                id = fee.id,
                invoiceNumber = fee.id,
                studentId = fee.studentId,
                studentName = fee.studentName,
                cohort = if (fee.classGrade.isNotBlank()) "${fee.classGrade}-${fee.division}" else "Standard",
                title = "Tuition & Academic Dues",
                totalAmount = fee.totalFee,
                paidAmount = fee.paidAmount,
                dueDate = fee.dueDate,
                issueDate = "Not recorded",
                status = fee.status,
                paymentChannel = "Razorpay Test (server verified)",
                description = "Tuition & Academic Dues"
              )
            }
          }
        }
      }
      launch { source.getAssignmentsFlow().collect { _assignments.value = it } }
      launch { source.getStudyMaterialsFlow().collect { _studyMaterials.value = it } }
      launch { source.getNoticesFlow().collect { _notices.value = it } }
      launch { source.getLeaveRequestsFlow().collect { _leaveRequests.value = it } }
      launch { source.getReportCardsFlow().collect { _reportCards.value = it } }
      launch { source.getExamSchedulesFlow().collect { _examSchedules.value = it } }
      launch { source.getTimetableFlow().collect { _timetable.value = it } }
      launch { source.getEventsFlow().collect { _events.value = it } }
      launch { source.getTransportRoutesFlow().collect { _transportRoutes.value = it } }
      launch { source.getLibraryBooksFlow().collect { _libraryBooks.value = it } }
      launch { source.getInventoryFlow().collect { _inventory.value = it } }
      launch { source.getNotificationsFlow().collect { _notifications.value = it } }
      launch {
        source.getClassAttendanceFlow().collect { map ->
          if (map.isNotEmpty() || source is SupabaseDataSource) {
            _classAttendance.value = map
          }
        }
      }
    }
  }

  fun setDataMode(mode: DataMode) {
    if (_dataMode.value == DataMode.SUPABASE_DEMO && mode != DataMode.SUPABASE_DEMO) {
      repositoryScope.launch { supabaseDataSource.logout() }
    }
    AppEnvironment.setMode(mode)
    _dataMode.value = mode
    bindDataSource()
  }

  // ========================================================================
  // Role Switcher / Authentication
  // All auth operations delegate to the active DataSource. The DataSource
  // updates its internal user flow, which bindDataSource() propagates here.
  // ========================================================================

  /**
   * Commits the chosen persona before [onComplete] can advance the UI.
   * This prevents demo login from opening the previous role's dashboard.
   */
  fun switchRole(role: UserRole, onComplete: (() -> Unit)? = null) {
    repositoryScope.launch {
      activeSource.switchRole(role)
      val switchedUser = activeSource.getCurrentUserFlow().first { it.role == role }
      _currentUser.value = switchedUser
      updateSelectedStudent()
      onComplete?.invoke()
    }
  }

  fun loginCustom(email: String, role: UserRole, name: String) {
    repositoryScope.launch {
      activeSource.login(email, role, name)
    }
  }

  fun loginWithSupabase(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
    repositoryScope.launch {
      try {
        check(_dataMode.value == DataMode.SUPABASE_DEMO)
        supabaseDataSource.authenticate(email, password)
        if (_dataMode.value != DataMode.SUPABASE_DEMO) {
          supabaseDataSource.logout()
          onResult(false,"Mode changed. Please sign in again.")
          return@launch
        }
        _currentUser.value = supabaseDataSource.getCurrentUserFlow().first()
        updateSelectedStudent()
        onResult(true, null)
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        onResult(false, e.message ?: "Supabase sign-in failed.")
      }
    }
  }

  fun logout() {
    repositoryScope.launch {
      activeSource.logout()
      _selectedStudentId.value = ""
      if (_dataMode.value == DataMode.SUPABASE_DEMO) {
        _students.value = emptyList(); _teachers.value = emptyList(); _feeRecords.value = emptyList()
        _assignments.value = emptyList(); _studyMaterials.value = emptyList(); _notices.value = emptyList()
        _leaveRequests.value = emptyList(); _reportCards.value = emptyList(); _examSchedules.value = emptyList()
        _timetable.value = emptyList(); _events.value = emptyList(); _transportRoutes.value = emptyList()
        _libraryBooks.value = emptyList(); _inventory.value = emptyList(); _notifications.value = emptyList()
        _classAttendance.value = emptyMap(); _auditLogs.value = emptyList(); _invoices.value = emptyList(); _gradebookEntries.value = emptyList()
      }
    }
  }

  // ========================================================================
  // Student CRUD — Pure delegation to DataSource
  // DataSource handles: state update + Firestore persistence + notifications
  // bindDataSource() collectors propagate changes to Repository StateFlows
  // ========================================================================

  suspend fun enrollStudent(student: Student, feeAmount: Long) {
    if (_dataMode.value == DataMode.SUPABASE_DEMO) supabaseDataSource.enrollStudent(student,feeAmount)
    else activeSource.addStudent(student)
  }

  fun addStudent(student: Student) {
    repositoryScope.launch {
      activeSource.addStudent(student)
    }
  }

  fun updateStudent(updatedStudent: Student) {
    repositoryScope.launch {
      activeSource.updateStudent(updatedStudent)
    }
  }

  fun deleteStudent(studentId: String) {
    repositoryScope.launch {
      activeSource.deleteStudent(studentId)
    }
  }

  // ========================================================================
  // Teacher CRUD — Pure delegation
  // ========================================================================

  fun addTeacher(teacher: Teacher) {
    repositoryScope.launch {
      activeSource.addTeacher(teacher)
    }
  }

  fun updateTeacher(updatedTeacher: Teacher) {
    repositoryScope.launch {
      activeSource.updateTeacher(updatedTeacher)
    }
  }

  // ========================================================================
  // Attendance — Pure delegation + read-only calculations
  // ========================================================================

  fun saveClassAttendance(
    classGrade: String,
    division: String,
    date: String,
    markedBy: String,
    studentList: List<StudentAttendance>
  ) {
    repositoryScope.launch {
      activeSource.saveClassAttendance(classGrade, division, date, markedBy, studentList)
    }
  }

  /**
   * Returns today's attendance status for a student by searching all class attendance records.
   */
  fun getStudentTodayAttendance(studentId: String): AttendanceStatus {
    for ((_, record) in _classAttendance.value) {
      val found = record.studentList.firstOrNull { it.studentId == studentId }
      if (found != null) {
        return found.status
      }
    }
    return AttendanceStatus.PRESENT
  }

  /**
   * Calculates attendance percentage from actual ClassAttendanceRecords.
   * This is the CORRECT calculation — counts actual PRESENT+LATE / total records.
   */
  fun calculateStudentAttendancePercentage(studentId: String, defaultBaseline: Double = 95.0): Double {
    val studentAttendanceHistory = _classAttendance.value.values.mapNotNull { rec ->
      rec.studentList.firstOrNull { it.studentId == studentId }
    }
    if (studentAttendanceHistory.isEmpty()) {
      return if (_dataMode.value == DataMode.SUPABASE_DEMO) 0.0 else defaultBaseline
    }
    val attendedCount = studentAttendanceHistory.count {
      it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE
    }
    val totalCount = studentAttendanceHistory.size
    val computed = (attendedCount.toDouble() / totalCount.toDouble()) * 100.0
    return String.format(Locale.US, "%.1f", computed).toDouble()
  }

  fun getOverallSchoolAttendanceRate(): Double {
    val records = _classAttendance.value.values
    if (records.isEmpty()) return if (_dataMode.value == DataMode.SUPABASE_DEMO) 0.0 else 96.4
    val totalStudents = records.sumOf { it.totalStudents }
    val totalPresent = records.sumOf { it.presentCount + it.lateCount }
    return if (totalStudents > 0) (totalPresent.toDouble() / totalStudents.toDouble()) * 100.0 else if (_dataMode.value == DataMode.SUPABASE_DEMO) 0.0 else 96.4
  }

  fun getTodayStaffAttendanceRate(): Double {
    val total = _teachers.value.size
    return if (total > 0) 100.0 else 0.0
  }

  // ========================================================================
  // Leave Actions — Pure delegation
  // ========================================================================

  fun applyLeave(
    applicantName: String,
    applicantRole: UserRole,
    applicantId: String,
    classOrDept: String,
    leaveType: String,
    startDate: String,
    endDate: String,
    daysCount: Int,
    reason: String
  ) {
    repositoryScope.launch {
      activeSource.applyLeave(
        applicantName,
        applicantRole,
        applicantId,
        classOrDept,
        leaveType,
        startDate,
        endDate,
        daysCount,
        reason
      )
    }
  }

  fun approveLeave(leaveId: String, remark: String = "Approved by Principal") {
    repositoryScope.launch {
      activeSource.updateLeaveStatus(leaveId, LeaveStatus.APPROVED, remark)
    }
  }

  fun rejectLeave(leaveId: String, remark: String = "Rejected due to scheduling conflicts") {
    repositoryScope.launch {
      activeSource.updateLeaveStatus(leaveId, LeaveStatus.REJECTED, remark)
    }
  }

  // ========================================================================
  // Fee Payment Processing — Pure delegation
  // The DataSource handles: transaction creation, balance update, receipt,
  // student record sync, notification generation, and Firestore persistence.
  // ========================================================================

  fun processFeePayment(
    studentId: String,
    amountPaid: Long,
    paymentMethod: String,
    feeHead: String = "Term 2 Tuition & Activity Dues",
    razorpayPaymentId: String? = null,
    razorpayOrderId: String? = null,
    razorpaySignature: String? = null
  ): FeePaymentTransaction {
    check(_dataMode.value == DataMode.DEMO) { "Payments are disabled in the Supabase demo." }
    val correctAmount = _feeRecords.value.firstOrNull { it.studentId == studentId }?.pendingAmount ?: amountPaid
    val finalTxnId = razorpayPaymentId ?: "TXN-REV-${(100000..999999).random()}"

    val txn = FeePaymentTransaction(
      transactionId = finalTxnId,
      receiptNo = "REC-2026-${(1000..9999).random()}",
      amount = correctAmount,
      date = getCurrentFormattedDate(),
      method = paymentMethod,
      status = "SUCCESS",
      feeHead = feeHead
    )
    repositoryScope.launch {
      activeSource.processFeePayment(
        studentId = studentId,
        amountPaid = correctAmount,
        paymentMethod = paymentMethod,
        feeHead = feeHead,
        razorpayPaymentId = razorpayPaymentId,
        razorpayOrderId = razorpayOrderId,
        razorpaySignature = razorpaySignature
      )
    }
    return txn
  }

  // ========================================================================
  // Assignments — Pure delegation
  // ========================================================================

  suspend fun createAssignmentConfirmed(assignment: HomeworkAssignment) = activeSource.createAssignment(assignment)
  suspend fun publishNoticeConfirmed(notice: SchoolNotice) = activeSource.publishNotice(notice)
  suspend fun readNotificationConfirmed(id: String) = activeSource.markNotificationAsRead(id)
  suspend fun readAllNotificationsConfirmed() = activeSource.markAllNotificationsAsRead()
  suspend fun submitAssignmentConfirmed(id: String) = activeSource.toggleAssignmentCompletion(id)

  fun createAssignment(assignment: HomeworkAssignment) {
    repositoryScope.launch {
      activeSource.createAssignment(assignment)
    }
  }

  fun toggleAssignmentCompletion(assignmentId: String) {
    repositoryScope.launch {
      activeSource.toggleAssignmentCompletion(assignmentId)
    }
  }

  private suspend fun confirmedStaffAction(action: suspend (ErpDataSource) -> Unit) {
    val actor = currentUser.value
    val mode = dataMode.value
    check(actor.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)) { "Staff access required." }
    action(activeSource)
    check(currentUser.value.id == actor.id && dataMode.value == mode) { "Account changed. Reload this workspace." }
  }

  suspend fun saveMarksConfirmed(studentId: String, term: String, subject: String, obtained: Int, maximum: Int) = confirmedStaffAction {
    require(term.trim().isNotEmpty() && subject.trim().isNotEmpty() && maximum in 1..10000 && obtained in 0..maximum)
    it.saveStudentMarks(studentId, term.trim(), subject.trim(), obtained, maximum)
  }

  suspend fun publishResultsConfirmed(studentId: String, term: String, published: Boolean) = confirmedStaffAction {
    it.publishReportCard(studentId, term, published)
  }

  suspend fun decideLeaveConfirmed(id: String, status: LeaveStatus, remark: String) = confirmedStaffAction {
    check(currentUser.value.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN))
    it.updateLeaveStatus(id, status, remark)
  }

  fun saveStudentMarks(
    studentId: String,
    term: String,
    subjectName: String,
    obtainedMarks: Int,
    maxMarks: Int
  ) {
    repositoryScope.launch {
      activeSource.saveStudentMarks(studentId, term, subjectName, obtainedMarks, maxMarks)
    }
  }

  fun saveCheckedPaper(
    studentId: String,
    term: String,
    subjectName: String,
    checkedPaperUrl: String
  ) {
    repositoryScope.launch {
      activeSource.saveCheckedPaper(studentId, term, subjectName, checkedPaperUrl)
    }
  }

  fun saveAnswerKey(
    classGrade: String,
    term: String,
    subjectName: String,
    answerKeyUrl: String
  ) {
    repositoryScope.launch {
      activeSource.saveAnswerKey(classGrade, term, subjectName, answerKeyUrl)
    }
  }

  // ========================================================================
  // Study Material — Pure delegation
  // ========================================================================

  fun uploadStudyMaterial(material: StudyMaterial) {
    repositoryScope.launch {
      activeSource.uploadStudyMaterial(material)
    }
  }

  // ------------------------------------------------------------------------
  // Uploads require an authenticated Supabase session.
  suspend fun uploadAttachment(
    context: android.content.Context,
    folder: String,
    sourceUri: android.net.Uri,
    displayName: String?,
    mimeType: String
  ): DocumentFiles.UploadResult {
    if (_dataMode.value == DataMode.SUPABASE_DEMO) {
      return SupabaseFileStore.upload(context, sourceUri, displayName, mimeType, _currentUser.value, supabaseDataSource.sessionId())
    }
    throw IllegalStateException(
      "Local demo does not upload files. Sign in to Supabase demo to store a document."
    )
  }

  // ========================================================================
  // Notices — Pure delegation
  // ========================================================================

  fun publishNotice(notice: SchoolNotice) {
    repositoryScope.launch {
      activeSource.publishNotice(notice)
    }
  }

  // ========================================================================
  // Library — Pure delegation
  // ========================================================================

  fun issueBook(bookId: String) {
    repositoryScope.launch {
      activeSource.issueBook(bookId)
    }
  }

  fun returnBook(bookId: String) {
    repositoryScope.launch {
      activeSource.returnBook(bookId)
    }
  }

  fun publishReportCard(studentId: String, term: String, published: Boolean) {
    repositoryScope.launch {
      activeSource.publishReportCard(studentId, term, published)
    }
  }

  // ========================================================================
  // Notification Handling — Pure delegation
  // ========================================================================

  fun addNotification(
    title: String,
    message: String,
    category: NotificationCategory,
    targetRole: UserRole? = null
  ) {
    repositoryScope.launch {
      activeSource.addNotification(title, message, category, targetRole)
    }
  }

  fun markNotificationAsRead(id: String) {
    repositoryScope.launch {
      activeSource.markNotificationAsRead(id)
    }
  }

  fun markAllNotificationsAsRead() {
    repositoryScope.launch {
      activeSource.markAllNotificationsAsRead()
    }
  }

  private fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
  }

  companion object {
    @Volatile
    private var instance: ErpDataRepository? = null

    fun getInstance(): ErpDataRepository {
      return instance ?: synchronized(this) {
        instance ?: ErpDataRepository().also { instance = it }
      }
    }
  }
}
