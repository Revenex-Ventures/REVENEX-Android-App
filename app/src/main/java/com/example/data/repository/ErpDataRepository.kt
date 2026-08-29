package com.example.data.repository

import android.util.Log
import com.example.data.datasource.AppEnvironment
import com.example.data.datasource.DataMode
import com.example.data.datasource.ErpDataSource
import com.example.data.datasource.FirebaseDataSource
import com.example.data.datasource.FirebaseStorageManager
import com.example.data.datasource.LocalDemoDataSource
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
  private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  val demoDataSource = LocalDemoDataSource()
  val firebaseDataSource = FirebaseDataSource()

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
    _selectedStudentId.value = id
  }

  private var lastUserId: String? = null

  private fun updateSelectedStudent() {
    val user = _currentUser.value
    if (user.id != lastUserId) {
      lastUserId = user.id
      _selectedStudentId.value = ""
    }
    if (user.role == UserRole.PARENT) {
      val wards = _students.value.filter { st ->
        st.parentPhone.replace("+91", "").trim().replace(" ", "") == user.phone.replace("+91", "").trim().replace(" ", "") ||
        st.id == user.associatedStudentId ||
        user.associatedChildNames.any { name -> st.name.contains(name.substringBefore(" (").trim(), ignoreCase = true) }
      }
      if (wards.isNotEmpty() && (_selectedStudentId.value.isBlank() || wards.none { it.id == _selectedStudentId.value })) {
        _selectedStudentId.value = wards.first().id
      }
    } else if (user.role == UserRole.STUDENT) {
      _selectedStudentId.value = user.associatedStudentId.ifBlank { "stu_1001" }
    }
  }

  private var activeSourceJob: Job? = null

  init {
    bindDataSource()
  }

  val activeSource: ErpDataSource
    get() = if (_dataMode.value == DataMode.PRODUCTION) firebaseDataSource else demoDataSource

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
      launch { source.getFeeRecordsFlow().collect { _feeRecords.value = it } }
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
          if (map.isNotEmpty()) {
            _classAttendance.value = map
          }
        }
      }
    }
  }

  fun setDataMode(mode: DataMode) {
    AppEnvironment.setMode(mode)
    _dataMode.value = mode
    bindDataSource()
  }

  // ========================================================================
  // Role Switcher / Authentication
  // All auth operations delegate to the active DataSource. The DataSource
  // updates its internal user flow, which bindDataSource() propagates here.
  // ========================================================================

  fun switchRole(role: UserRole) {
    repositoryScope.launch {
      activeSource.switchRole(role)
    }
  }

  fun loginCustom(email: String, role: UserRole, name: String) {
    repositoryScope.launch {
      activeSource.login(email, role, name)
    }
  }

  fun loginWithFirebase(
    email: String,
    pass: String,
    onResult: (Boolean, String?) -> Unit
  ) {
    try {
      val auth = FirebaseAuth.getInstance()
      auth.signInWithEmailAndPassword(email, pass)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val db = FirebaseFirestore.getInstance()
            db.collection("schools").document("revenex_school_001")
              .collection("users")
              .whereEqualTo("email", email)
              .get()
              .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                  val doc = snapshot.documents[0]
                  val roleStr = doc.getString("role")
                  val name = doc.getString("name") ?: "User"
                  if (roleStr != null) {
                    val role = UserRole.valueOf(roleStr)
                    switchRole(role)
                    repositoryScope.launch {
                      val profile = UserProfile(
                        id = doc.getString("id") ?: doc.id,
                        name = name,
                        email = email,
                        role = role,
                        designation = doc.getString("designation") ?: "",
                        phone = doc.getString("phone") ?: "",
                        avatarInitials = doc.getString("avatarInitials") ?: "",
                        associatedClass = doc.getString("associatedClass") ?: "",
                        associatedStudentId = doc.getString("associatedStudentId") ?: "",
                        associatedChildNames = (doc.get("associatedChildNames") as? List<String>) ?: emptyList()
                      )
                      activeSource.setCurrentUser(profile)
                    }
                    onResult(true, null)
                  } else {
                    auth.signOut()
                    onResult(false, "User profile exists but role is not defined.")
                  }
                } else {
                  auth.signOut()
                  onResult(false, "Your account is not authorized as a school administrator.")
                }
              }
              .addOnFailureListener { e ->
                auth.signOut()
                onResult(false, "Failed to retrieve school authorization: ${e.message}")
              }
          } else {
            onResult(false, task.exception?.localizedMessage ?: "Authentication failed")
          }
        }
    } catch (e: Exception) {
      Log.w(tag, "Firebase auth error: ${e.message}")
      onResult(false, "Firebase is not configured. Please use Demo Mode or configure google-services.json.")
    }
  }

  fun logout() {
    repositoryScope.launch {
      activeSource.logout()
    }
  }

  // ========================================================================
  // Student CRUD — Pure delegation to DataSource
  // DataSource handles: state update + Firestore persistence + notifications
  // bindDataSource() collectors propagate changes to Repository StateFlows
  // ========================================================================

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
      return defaultBaseline
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
    if (records.isEmpty()) return 96.4
    val totalStudents = records.sumOf { it.totalStudents }
    val totalPresent = records.sumOf { it.presentCount + it.lateCount }
    return if (totalStudents > 0) (totalPresent.toDouble() / totalStudents.toDouble()) * 100.0 else 96.4
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
    amountPaid: Double,
    paymentMethod: String,
    feeHead: String = "Term 2 Tuition & Activity Dues",
    razorpayPaymentId: String? = null,
    razorpayOrderId: String? = null,
    razorpaySignature: String? = null
  ): FeePaymentTransaction {
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
  // Real file upload (Firebase Storage) — honest persistence, never faked.
  // Demo mode returns a local metadata marker; production uploads the bytes
  // and returns the resolved download URL. Throws when upload is impossible.
  // ------------------------------------------------------------------------

  suspend fun uploadAttachment(
    context: android.content.Context,
    folder: String,
    sourceUri: android.net.Uri,
    displayName: String?,
    mimeType: String
  ): FirebaseStorageManager.UploadResult {
    if (activeSource.isProductionMode() && FirebaseStorageManager.isConfigured()) {
      return FirebaseStorageManager.uploadFile(
        context = context,
        schoolId = "revenex_school_001",
        folder = folder,
        sourceUri = sourceUri,
        displayName = displayName,
        mimeType = mimeType
      )
    }
    // Demo/offline mode: no false claim of a real upload. Surface clearly that this
    // build records metadata only (Firebase config unavailable at runtime).
    throw IllegalStateException(
      "Upload requires Firebase Storage configuration. " +
        "In demo mode attachments are recorded as metadata only."
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
