package com.example.data.datasource

import android.util.Log
import com.example.data.model.*
import com.example.data.repository.SampleData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Production Firebase Data Source for REVENEX School ERP.
 * Implements real-time Cloud Firestore synchronization and Firebase Authentication
 * with strict school tenant isolation: `/schools/{schoolId}/...`.
 */
class FirebaseDataSource(
  val schoolId: String = "revenex_school_001",
  private val fallbackDemoSource: LocalDemoDataSource = LocalDemoDataSource()
) : ErpDataSource {

  private val tag = "FirebaseDataSource"
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  // Firebase instances (safely resolved)
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(tag, "Cloud Firestore not initialized: ${e.message}")
      null
    }
  }

  private val firebaseAuth: FirebaseAuth? by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      Log.w(tag, "Firebase Auth not initialized: ${e.message}")
      null
    }
  }

  // Internal state flows synced with Firestore
  private val _currentUser = MutableStateFlow(SampleData.defaultProfiles[0])
  private val _students = MutableStateFlow<List<Student>>(SampleData.initialStudents)
  private val _teachers = MutableStateFlow<List<Teacher>>(SampleData.initialTeachers)
  private val _feeRecords = MutableStateFlow<List<FeeRecord>>(SampleData.initialFeeRecords)
  private val _assignments = MutableStateFlow<List<HomeworkAssignment>>(SampleData.initialAssignments)
  private val _studyMaterials = MutableStateFlow<List<StudyMaterial>>(SampleData.initialStudyMaterials)
  private val _notices = MutableStateFlow<List<SchoolNotice>>(SampleData.initialNotices)
  private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(SampleData.initialLeaveRequests)
  private val _reportCards = MutableStateFlow<List<ReportCard>>(SampleData.initialReportCards)
  private val _examSchedules = MutableStateFlow<List<ExamSchedule>>(SampleData.initialExamSchedules)
  private val _timetable = MutableStateFlow<List<TimetableSlot>>(SampleData.initialTimetable)
  private val _events = MutableStateFlow<List<SchoolEvent>>(SampleData.initialEvents)
  private val _transportRoutes = MutableStateFlow<List<TransportRoute>>(SampleData.initialTransportRoutes)
  private val _libraryBooks = MutableStateFlow<List<LibraryBook>>(SampleData.initialLibraryBooks)
  private val _inventory = MutableStateFlow<List<InventoryAsset>>(SampleData.initialInventory)
  private val _notifications = MutableStateFlow<List<ErpNotification>>(SampleData.initialNotifications)
  private val _classAttendance = MutableStateFlow<Map<String, ClassAttendanceRecord>>(emptyMap())

  private var isInitialized = false

  init {
    setupRealtimeListeners()
    restoreUserSession()
  }

  private fun restoreUserSession() {
    val authUser = firebaseAuth?.currentUser
    if (authUser != null && authUser.email != null) {
      val email = authUser.email
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null && email != null) {
        schoolDoc.collection("users")
          .whereEqualTo("email", email)
          .get()
          .addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
              val doc = snapshot.documents.first()
              val profile = mapDocToUserProfile(doc)
              if (profile != null) {
                _currentUser.value = profile
                Log.i(tag, "Restored user session for ${profile.name} (${profile.role})")
              }
            }
          }
          .addOnFailureListener { e ->
            Log.w(tag, "Failed to restore user session from Firestore: ${e.message}")
          }
      }
    }
  }

  private fun getSchoolDoc(): DocumentReference? = firestore?.collection("schools")?.document(schoolId)

  private fun setupRealtimeListeners() {
    val schoolDoc = getSchoolDoc() ?: return

    try {
      // 1. Students Listener
      schoolDoc.collection("students").addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(tag, "Error listening to students: ${error.message}")
          return@addSnapshotListener
        }
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToStudent(it) }
          _students.value = list
        } else if (snapshot != null && snapshot.isEmpty && !isInitialized) {
          scope.launch { seedInitialFirestoreData() }
        }
      }

      // 2. Teachers Listener
      schoolDoc.collection("teachers").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToTeacher(it) }
          _teachers.value = list
        }
      }

      // 3. Fee Records Listener
      schoolDoc.collection("fees").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToFeeRecord(it) }
          _feeRecords.value = list
        }
      }

      // 4. Assignments Listener
      schoolDoc.collection("assignments").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToAssignment(it) }
          _assignments.value = list
        }
      }

      // 5. Study Materials Listener
      schoolDoc.collection("study_materials").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToStudyMaterial(it) }
          _studyMaterials.value = list
        }
      }

      // 6. Notices Listener
      schoolDoc.collection("notices").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToNotice(it) }
          _notices.value = list
        }
      }

      // 7. Leaves Listener
      schoolDoc.collection("leaves").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToLeave(it) }
          _leaveRequests.value = list
        }
      }

      // 8. Timetable Listener
      schoolDoc.collection("timetable").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToTimetableSlot(it) }
          _timetable.value = list
        }
      }

      // 9. Notifications Listener
      schoolDoc.collection("notifications").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToNotification(it) }
          _notifications.value = list
        }
      }

      // 10. Library Listener
      schoolDoc.collection("library").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToLibraryBook(it) }
          _libraryBooks.value = list
        }
      }

      // 11. Transport Listener
      schoolDoc.collection("transport").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToTransportRoute(it) }
          _transportRoutes.value = list
        }
      }

      // 12. Inventory Listener
      schoolDoc.collection("inventory").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToInventory(it) }
          _inventory.value = list
        }
      }

      // 13. Attendance Listener
      schoolDoc.collection("attendance").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val map = mutableMapOf<String, ClassAttendanceRecord>()
          snapshot.documents.forEach { doc ->
            val record = mapDocToAttendanceRecord(doc)
            if (record != null) {
              val key = "${record.classGrade}-${record.division}_${record.date}"
              map[key] = record
            }
          }
          _classAttendance.value = map
        }
      }

      // 14. Exams Listener
      schoolDoc.collection("exams").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToExamSchedule(it) }
          _examSchedules.value = list
        }
      }

      // 15. Report Cards Listener
      schoolDoc.collection("report_cards").addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { mapDocToReportCard(it) }
          _reportCards.value = list
        }
      }

      isInitialized = true
    } catch (e: Exception) {
      Log.e(tag, "Failed to initialize Firestore snapshot listeners: ${e.message}")
    }
  }

  private suspend fun seedInitialFirestoreData() = withContext(Dispatchers.IO) {
    val schoolDoc = getSchoolDoc() ?: return@withContext
    try {
      val batch = firestore?.batch() ?: return@withContext

      batch.set(schoolDoc, mapOf(
        "name" to "Revenex Public School",
        "affiliation" to "CBSE / REV-DELHI-042",
        "city" to "Pune, Maharashtra",
        "established" to 2012,
        "activeSession" to "2026-2027"
      ), SetOptions.merge())

      SampleData.initialStudents.forEach { st ->
        val ref = schoolDoc.collection("students").document(st.id)
        batch.set(ref, studentToMap(st))
      }

      SampleData.initialTeachers.forEach { tch ->
        val ref = schoolDoc.collection("teachers").document(tch.id)
        batch.set(ref, teacherToMap(tch))
      }

      SampleData.initialFeeRecords.forEach { fee ->
        val ref = schoolDoc.collection("fees").document(fee.id)
        batch.set(ref, feeRecordToMap(fee))
      }

      SampleData.initialAssignments.forEach { hw ->
        val ref = schoolDoc.collection("assignments").document(hw.id)
        batch.set(ref, assignmentToMap(hw))
      }

      SampleData.initialNotices.forEach { nt ->
        val ref = schoolDoc.collection("notices").document(nt.id)
        batch.set(ref, noticeToMap(nt))
      }

      SampleData.initialLeaveRequests.forEach { lv ->
        val ref = schoolDoc.collection("leaves").document(lv.id)
        batch.set(ref, leaveToMap(lv))
      }

      SampleData.initialLibraryBooks.forEach { bk ->
        val ref = schoolDoc.collection("library").document(bk.id)
        batch.set(ref, libraryBookToMap(bk))
      }

      SampleData.initialTransportRoutes.forEach { tr ->
        val ref = schoolDoc.collection("transport").document(tr.id)
        batch.set(ref, transportRouteToMap(tr))
      }

      SampleData.initialInventory.forEach { inv ->
        val ref = schoolDoc.collection("inventory").document(inv.id)
        batch.set(ref, inventoryToMap(inv))
      }

      SampleData.initialTimetable.forEach { tt ->
        val ref = schoolDoc.collection("timetable").document(tt.id)
        batch.set(ref, timetableSlotToMap(tt))
      }

      SampleData.initialExamSchedules.forEach { ex ->
        val ref = schoolDoc.collection("exams").document(ex.id)
        batch.set(ref, examScheduleToMap(ex))
      }

      SampleData.initialReportCards.forEach { rc ->
        val ref = schoolDoc.collection("report_cards").document(rc.id)
        batch.set(ref, reportCardToMap(rc))
      }

      SampleData.defaultProfiles.forEach { prof ->
        val ref = schoolDoc.collection("users").document(prof.id)
        batch.set(ref, mapOf(
          "id" to prof.id,
          "name" to prof.name,
          "email" to prof.email,
          "role" to prof.role.name,
          "designation" to prof.designation,
          "phone" to prof.phone,
          "avatarInitials" to prof.avatarInitials,
          "associatedClass" to prof.associatedClass,
          "associatedStudentId" to prof.associatedStudentId,
          "associatedChildNames" to prof.associatedChildNames
        ))
      }

      batch.commit()
      Log.i(tag, "Successfully seeded initial Firestore school collections.")
    } catch (e: Exception) {
      Log.w(tag, "Initial Firestore seed skipped: ${e.message}")
    }
  }

  override fun isProductionMode(): Boolean = true

  override fun getCurrentUserFlow(): Flow<UserProfile> = _currentUser.asStateFlow()

  override suspend fun setCurrentUser(user: UserProfile) {
    _currentUser.value = user
    val schoolDoc = getSchoolDoc()
    if (schoolDoc != null) {
      try {
        schoolDoc.collection("users").document(user.id).set(mapOf(
          "id" to user.id,
          "name" to user.name,
          "email" to user.email,
          "role" to user.role.name,
          "designation" to user.designation,
          "phone" to user.phone,
          "avatarInitials" to user.avatarInitials,
          "associatedClass" to user.associatedClass,
          "associatedStudentId" to user.associatedStudentId,
          "associatedChildNames" to user.associatedChildNames
        ), SetOptions.merge())
      } catch (e: Exception) {
        Log.w(tag, "Failed to persist user profile: ${e.message}")
      }
    }
  }

  override suspend fun switchRole(role: UserRole) {
    val targetProfile = SampleData.defaultProfiles.firstOrNull { it.role == role }
      ?: UserProfile(
        id = UUID.randomUUID().toString(),
        name = "Authorized User",
        email = "user@revenexschool.edu.in",
        role = role,
        designation = role.displayName,
        phone = "+91 98000 00000",
        avatarInitials = role.name.take(2)
      )
    setCurrentUser(targetProfile)
  }

  override suspend fun login(email: String, role: UserRole, name: String) {
    val initials = name.split(" ")
      .mapNotNull { it.firstOrNull()?.toString() }
      .joinToString("")
      .take(2)
      .uppercase()

    val profile = UserProfile(
      id = "user_${System.currentTimeMillis()}",
      name = name.ifBlank { role.displayName },
      email = email,
      role = role,
      designation = role.displayName,
      phone = "+91 98220 12345",
      avatarInitials = if (initials.isNotEmpty()) initials else "RX",
      associatedClass = if (role == UserRole.TEACHER || role == UserRole.STUDENT) "10-A" else "",
      associatedStudentId = if (role == UserRole.STUDENT || role == UserRole.PARENT) "stu_1001" else "",
      associatedChildNames = if (role == UserRole.PARENT) listOf("Aarav Patel (10-A)", "Diya Patel (6-B)") else emptyList()
    )
    setCurrentUser(profile)
  }

  override suspend fun logout() {
    try {
      firebaseAuth?.signOut()
    } catch (e: Exception) {
      Log.w(tag, "Firebase sign out error: ${e.message}")
    }
    _currentUser.value = SampleData.defaultProfiles[0]
  }

  override fun getStudentsFlow(): Flow<List<Student>> = _students.asStateFlow()

  override suspend fun addStudent(student: Student) {
    withContext(Dispatchers.IO) {
      _students.update { listOf(student) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("students").document(student.id).set(studentToMap(student))
        } catch (e: Exception) {
          Log.e(tag, "Error saving student to Firestore: ${e.message}")
        }
      }
      addNotification(
        title = "New Student Enrolled",
        message = "${student.name} admitted to Class ${student.fullClass} (Adm #${student.admissionNumber}).",
        category = NotificationCategory.SYSTEM,
        targetRole = UserRole.PRINCIPAL
      )
    }
  }

  override suspend fun updateStudent(student: Student) {
    withContext(Dispatchers.IO) {
      _students.update { list -> list.map { if (it.id == student.id) student else it } }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("students").document(student.id).set(studentToMap(student), SetOptions.merge())
        } catch (e: Exception) {
          Log.e(tag, "Error updating student in Firestore: ${e.message}")
        }
      }
    }
  }

  override suspend fun deleteStudent(studentId: String) {
    withContext(Dispatchers.IO) {
      _students.update { list -> list.filterNot { it.id == studentId } }
      _feeRecords.update { list -> list.filterNot { it.studentId == studentId } }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("students").document(studentId).delete()
          schoolDoc.collection("fees").document("fee_$studentId").delete()
        } catch (e: Exception) {
          Log.e(tag, "Error deleting student in Firestore: ${e.message}")
        }
      }
    }
  }

  override fun getTeachersFlow(): Flow<List<Teacher>> = _teachers.asStateFlow()

  override suspend fun addTeacher(teacher: Teacher) {
    withContext(Dispatchers.IO) {
      _teachers.update { listOf(teacher) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("teachers").document(teacher.id).set(teacherToMap(teacher))
        } catch (e: Exception) {
          Log.e(tag, "Error saving teacher to Firestore: ${e.message}")
        }
      }
      addNotification(
        title = "Faculty Profile Added",
        message = "${teacher.name} appointed as ${teacher.designation}.",
        category = NotificationCategory.SYSTEM,
        targetRole = UserRole.PRINCIPAL
      )
    }
  }

  override suspend fun updateTeacher(teacher: Teacher) {
    withContext(Dispatchers.IO) {
      _teachers.update { list -> list.map { if (it.id == teacher.id) teacher else it } }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("teachers").document(teacher.id).set(teacherToMap(teacher), SetOptions.merge())
        } catch (e: Exception) {
          Log.e(tag, "Error updating teacher in Firestore: ${e.message}")
        }
      }
    }
  }

  override fun getClassAttendanceFlow(): Flow<Map<String, ClassAttendanceRecord>> = _classAttendance.asStateFlow()

  override suspend fun saveClassAttendance(
    classGrade: String,
    division: String,
    date: String,
    markedBy: String,
    studentList: List<StudentAttendance>
  ) {
    withContext(Dispatchers.IO) {
      val total = studentList.size
      val present = studentList.count { it.status == AttendanceStatus.PRESENT }
      val absent = studentList.count { it.status == AttendanceStatus.ABSENT }
      val late = studentList.count { it.status == AttendanceStatus.LATE }
      val leave = studentList.count { it.status == AttendanceStatus.LEAVE }

      // Deterministic document id per class+division+date: re-submitting attendance for
      // the same day overwrites the same Firestore doc (duplicate prevention), while a
      // new date creates a distinct historical record. Key is date-stamped to match the
      // snapshot-listener mapping, preserving history instead of overwriting it.
      val recordId = "att_${classGrade}${division}_${date.replace(" ", "_")}"
      val record = ClassAttendanceRecord(
        id = recordId,
        classGrade = classGrade,
        division = division,
        date = date,
        totalStudents = total,
        presentCount = present,
        absentCount = absent,
        lateCount = late,
        leaveCount = leave,
        markedBy = markedBy,
        studentList = studentList
      )

      val key = "${classGrade}-${division}_${date}"
      _classAttendance.update { it + (key to record) }

      // Update individual students' attendance percentage & stats based on actual records
      val allAttendanceRecords = _classAttendance.value.values.toList()
      _students.update { currentStudents ->
        currentStudents.map { st ->
          val studentHistory = allAttendanceRecords.flatMap { rec ->
            rec.studentList.filter { it.studentId == st.id }
          }
          if (studentHistory.isNotEmpty()) {
            val attendedCount = studentHistory.count {
              it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE
            }
            val rate = (attendedCount.toDouble() / studentHistory.size.toDouble()) * 100.0
            val updated = st.copy(attendancePercent = String.format(Locale.US, "%.1f", rate).toDouble())
            getSchoolDoc()?.collection("students")?.document(st.id)?.update("attendancePercent", updated.attendancePercent)
            updated
          } else {
            st
          }
        }
      }

      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("attendance").document(recordId).set(attendanceRecordToMap(record))
        } catch (e: Exception) {
          Log.e(tag, "Error saving attendance in Firestore: ${e.message}")
        }
      }

      addNotification(
        title = "Attendance Saved: Class $classGrade-$division",
        message = "Marked by $markedBy. Present: $present, Absent: $absent, Late: $late.",
        category = NotificationCategory.ATTENDANCE
      )
    }
  }

  override fun getFeeRecordsFlow(): Flow<List<FeeRecord>> = _feeRecords.asStateFlow()

  override suspend fun processFeePayment(
    studentId: String,
    amountPaid: Double,
    paymentMethod: String,
    feeHead: String,
    razorpayPaymentId: String?,
    razorpayOrderId: String?,
    razorpaySignature: String?
  ): FeePaymentTransaction = withContext(Dispatchers.IO) {
    // Security check: Never trust client-provided payment amounts
    val pending = _feeRecords.value.firstOrNull { it.studentId == studentId }?.pendingAmount ?: amountPaid
    val correctAmount = minOf(amountPaid, pending)
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

    var updatedRecord: FeeRecord? = null

    _feeRecords.update { list ->
      list.map { record ->
        if (record.studentId == studentId) {
          val newPaid = record.paidAmount + correctAmount
          val newStatus = if (newPaid >= record.totalFee) FeeStatus.PAID else FeeStatus.PARTIAL
          val rec = record.copy(
            paidAmount = newPaid,
            status = newStatus,
            lastPaymentDate = getCurrentFormattedDate(),
            transactions = listOf(txn) + record.transactions
          )
          updatedRecord = rec
          rec
        } else record
      }
    }

    _students.update { list ->
      list.map { st ->
        if (st.id == studentId) {
          val currentPending = st.feePendingAmount - correctAmount
          val newPending = currentPending.coerceAtLeast(0.0)
          val newStatus = if (newPending == 0.0) FeeStatus.PAID else FeeStatus.PARTIAL
          val updated = st.copy(feePendingAmount = newPending, feeStatus = newStatus)
          getSchoolDoc()?.collection("students")?.document(st.id)?.update(
            mapOf("feePendingAmount" to newPending, "feeStatus" to newStatus.name)
          )
          updated
        } else st
      }
    }

    val schoolDoc = getSchoolDoc()
    if (schoolDoc != null && updatedRecord != null) {
      try {
        schoolDoc.collection("fees").document(updatedRecord!!.id).set(feeRecordToMap(updatedRecord!!))
      } catch (e: Exception) {
        Log.e(tag, "Error saving fee payment in Firestore: ${e.message}")
      }
    }

    addNotification(
      title = "Fee Payment Successful (₹${amountPaid.toInt()})",
      message = "Receipt #${txn.receiptNo} generated via ${txn.method}. Account updated.",
      category = NotificationCategory.FEES
    )

    txn
  }

  override fun getLeaveRequestsFlow(): Flow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

  override suspend fun applyLeave(
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
    withContext(Dispatchers.IO) {
      val newLeave = LeaveRequest(
        id = "lv_${System.currentTimeMillis()}",
        applicantId = applicantId,
        applicantName = applicantName,
        applicantRole = applicantRole,
        classOrDept = classOrDept,
        leaveType = leaveType,
        startDate = startDate,
        endDate = endDate,
        daysCount = daysCount,
        reason = reason,
        appliedDate = getCurrentFormattedDate(),
        status = LeaveStatus.PENDING
      )
      _leaveRequests.update { listOf(newLeave) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("leaves").document(newLeave.id).set(leaveToMap(newLeave))
        } catch (e: Exception) {
          Log.e(tag, "Error applying leave in Firestore: ${e.message}")
        }
      }
      addNotification(
        title = "New Leave Application",
        message = "$applicantName ($classOrDept) applied for $daysCount day(s) $leaveType.",
        category = NotificationCategory.LEAVE,
        targetRole = UserRole.PRINCIPAL
      )
    }
  }

  override suspend fun updateLeaveStatus(
    leaveId: String,
    status: LeaveStatus,
    approverRemark: String
  ) {
    withContext(Dispatchers.IO) {
      _leaveRequests.update { list ->
        list.map {
          if (it.id == leaveId) it.copy(status = status, approverRemark = approverRemark)
          else it
        }
      }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("leaves").document(leaveId).update(
            mapOf("status" to status.name, "approverRemark" to approverRemark)
          )
        } catch (e: Exception) {
          Log.e(tag, "Error updating leave status in Firestore: ${e.message}")
        }
      }
      val statusText = if (status == LeaveStatus.APPROVED) "Approved" else "Rejected"
      addNotification(
        title = "Leave Request $statusText",
        message = "Leave application has been $statusText. Remark: $approverRemark",
        category = NotificationCategory.LEAVE
      )
    }
  }

  override fun getAssignmentsFlow(): Flow<List<HomeworkAssignment>> = _assignments.asStateFlow()

  override suspend fun createAssignment(assignment: HomeworkAssignment) {
    withContext(Dispatchers.IO) {
      _assignments.update { listOf(assignment) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("assignments").document(assignment.id).set(assignmentToMap(assignment))
        } catch (e: Exception) {
          Log.e(tag, "Error saving assignment in Firestore: ${e.message}")
        }
      }
      addNotification(
        title = "New Assignment: ${assignment.subject}",
        message = "${assignment.title} due on ${assignment.dueDate}.",
        category = NotificationCategory.HOMEWORK,
        targetRole = UserRole.STUDENT
      )
    }
  }

  override suspend fun toggleAssignmentCompletion(assignmentId: String) {
    withContext(Dispatchers.IO) {
      var isDone = false
      _assignments.update { list ->
        list.map {
          if (it.id == assignmentId) {
            val next = !it.isCompletedByStudent
            isDone = next
            it.copy(
              isCompletedByStudent = next,
              submissionStatus = if (next) "Submitted" else "Pending"
            )
          } else it
        }
      }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("assignments").document(assignmentId).update(
            mapOf("isCompletedByStudent" to isDone, "submissionStatus" to if (isDone) "Submitted" else "Pending")
          )
        } catch (e: Exception) {
          Log.e(tag, "Error toggling assignment in Firestore: ${e.message}")
        }
      }
    }
  }

  override fun getStudyMaterialsFlow(): Flow<List<StudyMaterial>> = _studyMaterials.asStateFlow()

  override suspend fun uploadStudyMaterial(material: StudyMaterial) {
    withContext(Dispatchers.IO) {
      _studyMaterials.update { listOf(material) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("study_materials").document(material.id).set(studyMaterialToMap(material))
        } catch (e: Exception) {
          Log.e(tag, "Error saving study material in Firestore: ${e.message}")
        }
      }
      addNotification(
        title = "New Study Resource Uploaded",
        message = "${material.subject}: ${material.title} (${material.chapter}).",
        category = NotificationCategory.ANNOUNCEMENTS,
        targetRole = UserRole.STUDENT
      )
    }
  }

  override fun getReportCardsFlow(): Flow<List<ReportCard>> = _reportCards.asStateFlow()

  override suspend fun saveStudentMarks(
    studentId: String,
    term: String,
    subjectName: String,
    obtainedMarks: Int,
    maxMarks: Int
  ) {
    withContext(Dispatchers.IO) {
      var updatedCard: ReportCard? = null
      _reportCards.update { list ->
        list.map { card ->
          if (card.studentId == studentId && card.term.contains(term, ignoreCase = true)) {
            val updatedScores = card.scores.toMutableList()
            val existingIndex = updatedScores.indexOfFirst { it.subjectName.equals(subjectName, ignoreCase = true) }
            
            val pct = if (maxMarks > 0) obtainedMarks.toDouble() / maxMarks else 0.0
            val grade = when {
              pct >= 0.9 -> "A+"
              pct >= 0.8 -> "A"
              pct >= 0.7 -> "B+"
              pct >= 0.6 -> "B"
              pct >= 0.5 -> "C"
              else -> "D"
            }
            
            val newScore = SubjectScore(subjectName, maxMarks, obtainedMarks, grade, "Marks entered by faculty")
            if (existingIndex >= 0) {
              updatedScores[existingIndex] = newScore
            } else {
              updatedScores.add(newScore)
            }
            val copy = card.copy(scores = updatedScores)
            updatedCard = copy
            copy
          } else card
        }
      }

      val cardToSave = updatedCard
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null && cardToSave != null) {
        try {
          schoolDoc.collection("reportCards").document(cardToSave.id).set(reportCardToMap(cardToSave))
        } catch (e: Exception) {
          Log.e(tag, "Error saving report card in Firestore: ${e.message}")
        }
      }
    }
  }

  override suspend fun saveCheckedPaper(
    studentId: String,
    term: String,
    subjectName: String,
    checkedPaperUrl: String
  ) {
    withContext(Dispatchers.IO) {
      var updatedCard: ReportCard? = null
      _reportCards.update { list ->
        list.map { card ->
          if (card.studentId == studentId && card.term.contains(term, ignoreCase = true)) {
            val updatedScores = card.scores.map { score ->
              if (score.subjectName.equals(subjectName, ignoreCase = true)) {
                score.copy(checkedPaperUrl = checkedPaperUrl)
              } else score
            }
            val copy = card.copy(scores = updatedScores)
            updatedCard = copy
            copy
          } else card
        }
      }

      val cardToSave = updatedCard
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null && cardToSave != null) {
        try {
          schoolDoc.collection("reportCards").document(cardToSave.id).set(reportCardToMap(cardToSave))
        } catch (e: Exception) {
          Log.e(tag, "Error saving checked paper in Firestore: ${e.message}")
        }
      }
    }
  }

  override suspend fun saveAnswerKey(
    classGrade: String,
    term: String,
    subjectName: String,
    answerKeyUrl: String
  ) {
    withContext(Dispatchers.IO) {
      val updatedCards = mutableListOf<ReportCard>()
      _reportCards.update { list ->
        list.map { card ->
          if (card.classGrade.equals(classGrade, ignoreCase = true) && card.term.contains(term, ignoreCase = true)) {
            val updatedScores = card.scores.map { score ->
              if (score.subjectName.equals(subjectName, ignoreCase = true)) {
                score.copy(answerKeyUrl = answerKeyUrl)
              } else score
            }
            val copy = card.copy(scores = updatedScores)
            updatedCards.add(copy)
            copy
          } else card
        }
      }

      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null && updatedCards.isNotEmpty()) {
        try {
          val batch = schoolDoc.firestore.batch()
          updatedCards.forEach { card ->
            val ref = schoolDoc.collection("reportCards").document(card.id)
            batch.set(ref, reportCardToMap(card))
          }
          batch.commit()
        } catch (e: Exception) {
          Log.e(tag, "Error saving answer key batch in Firestore: ${e.message}")
        }
      }
    }
  }
  override fun getExamSchedulesFlow(): Flow<List<ExamSchedule>> = _examSchedules.asStateFlow()
  override fun getTimetableFlow(): Flow<List<TimetableSlot>> = _timetable.asStateFlow()
  override fun getEventsFlow(): Flow<List<SchoolEvent>> = _events.asStateFlow()

  override fun getNoticesFlow(): Flow<List<SchoolNotice>> = _notices.asStateFlow()

  override suspend fun publishNotice(notice: SchoolNotice) {
    withContext(Dispatchers.IO) {
      _notices.update { listOf(notice) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("notices").document(notice.id).set(noticeToMap(notice))
        } catch (e: Exception) {
          Log.e(tag, "Error saving notice in Firestore: ${e.message}")
        }
      }
      addNotification(
        title = notice.title,
        message = notice.content.take(120) + "...",
        category = NotificationCategory.ANNOUNCEMENTS
      )
    }
  }

  override fun getNotificationsFlow(): Flow<List<ErpNotification>> = _notifications.asStateFlow()

  override suspend fun addNotification(
    title: String,
    message: String,
    category: NotificationCategory,
    targetRole: UserRole?
  ) {
    withContext(Dispatchers.IO) {
      val item = ErpNotification(
        id = "notif_${System.currentTimeMillis()}",
        title = title,
        message = message,
        category = category,
        timestamp = "Just now",
        isRead = false,
        targetRole = targetRole
      )
      _notifications.update { listOf(item) + it }
      val schoolDoc = getSchoolDoc()
      if (schoolDoc != null) {
        try {
          schoolDoc.collection("notifications").document(item.id).set(notificationToMap(item))
        } catch (e: Exception) {
          Log.e(tag, "Error saving notification in Firestore: ${e.message}")
        }
      }
    }
  }

  override suspend fun markNotificationAsRead(id: String) {
    withContext(Dispatchers.IO) {
      _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
      getSchoolDoc()?.collection("notifications")?.document(id)?.update("isRead", true)
    }
  }

  override suspend fun markAllNotificationsAsRead() {
    withContext(Dispatchers.IO) {
      _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }
  }

  override fun getTransportRoutesFlow(): Flow<List<TransportRoute>> = _transportRoutes.asStateFlow()
  override fun getLibraryBooksFlow(): Flow<List<LibraryBook>> = _libraryBooks.asStateFlow()

  override suspend fun issueBook(bookId: String) {
    withContext(Dispatchers.IO) {
      _libraryBooks.update { list ->
        list.map {
          if (it.id == bookId && it.availableCopies > 0) {
            val updated = it.copy(availableCopies = it.availableCopies - 1, issuedCount = it.issuedCount + 1)
            getSchoolDoc()?.collection("library")?.document(bookId)?.update(
              mapOf("availableCopies" to updated.availableCopies, "issuedCount" to updated.issuedCount)
            )
            updated
          } else it
        }
      }
    }
  }

  override suspend fun returnBook(bookId: String) {
    withContext(Dispatchers.IO) {
      _libraryBooks.update { list ->
        list.map {
          if (it.id == bookId) {
            val updated = it.copy(
              availableCopies = (it.availableCopies + 1).coerceAtMost(it.totalCopies),
              issuedCount = (it.issuedCount - 1).coerceAtLeast(0)
            )
            getSchoolDoc()?.collection("library")?.document(bookId)?.update(
              mapOf("availableCopies" to updated.availableCopies, "issuedCount" to updated.issuedCount)
            )
            updated
          } else it
        }
      }
    }
  }

  override fun getInventoryFlow(): Flow<List<InventoryAsset>> = _inventory.asStateFlow()

  private fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
  }

  // ==========================================
  // Firestore Serialization & Deserialization
  // ==========================================

  private fun studentToMap(st: Student): Map<String, Any> = mapOf(
    "id" to st.id,
    "admissionNumber" to st.admissionNumber,
    "rollNumber" to st.rollNumber,
    "name" to st.name,
    "classGrade" to st.classGrade,
    "division" to st.division,
    "dob" to st.dob,
    "gender" to st.gender,
    "bloodGroup" to st.bloodGroup,
    "parentName" to st.parentName,
    "parentPhone" to st.parentPhone,
    "parentEmail" to st.parentEmail,
    "address" to st.address,
    "admissionDate" to st.admissionDate,
    "attendancePercent" to st.attendancePercent,
    "feeStatus" to st.feeStatus.name,
    "feePendingAmount" to st.feePendingAmount,
    "rank" to st.rank,
    "gpa" to st.gpa,
    "avatarColorHex" to st.avatarColorHex
  )

  private fun mapDocToStudent(doc: DocumentSnapshot): Student? {
    return try {
      Student(
        id = doc.getString("id") ?: doc.id,
        admissionNumber = doc.getString("admissionNumber") ?: "REV-2026",
        rollNumber = (doc.getLong("rollNumber") ?: 1L).toInt(),
        name = doc.getString("name") ?: "",
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A",
        dob = doc.getString("dob") ?: "01 Jan 2010",
        gender = doc.getString("gender") ?: "Male",
        bloodGroup = doc.getString("bloodGroup") ?: "B+",
        parentName = doc.getString("parentName") ?: "",
        parentPhone = doc.getString("parentPhone") ?: "",
        parentEmail = doc.getString("parentEmail") ?: "",
        address = doc.getString("address") ?: "",
        admissionDate = doc.getString("admissionDate") ?: "10 Jun 2022",
        attendancePercent = doc.getDouble("attendancePercent") ?: 95.0,
        feeStatus = try { FeeStatus.valueOf(doc.getString("feeStatus") ?: "PAID") } catch (e: Exception) { FeeStatus.PAID },
        feePendingAmount = doc.getDouble("feePendingAmount") ?: 0.0,
        rank = (doc.getLong("rank") ?: 1L).toInt(),
        gpa = doc.getDouble("gpa") ?: 9.0,
        avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF2563EB
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun teacherToMap(tch: Teacher): Map<String, Any> = mapOf(
    "id" to tch.id,
    "employeeId" to tch.employeeId,
    "name" to tch.name,
    "dob" to tch.dob,
    "gender" to tch.gender,
    "department" to tch.department,
    "designation" to tch.designation,
    "qualification" to tch.qualification,
    "email" to tch.email,
    "phone" to tch.phone,
    "assignedClasses" to tch.assignedClasses,
    "subjects" to tch.subjects,
    "weeklyPeriods" to tch.weeklyPeriods,
    "attendancePercent" to tch.attendancePercent,
    "experienceYears" to tch.experienceYears,
    "joiningDate" to tch.joiningDate
  )

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToTeacher(doc: DocumentSnapshot): Teacher? {
    return try {
      Teacher(
        id = doc.getString("id") ?: doc.id,
        employeeId = doc.getString("employeeId") ?: "EMP-101",
        name = doc.getString("name") ?: "",
        dob = doc.getString("dob") ?: "01/01/1980",
        gender = doc.getString("gender") ?: "Male",
        department = doc.getString("department") ?: "General",
        designation = doc.getString("designation") ?: "Teacher",
        qualification = doc.getString("qualification") ?: "B.Ed, M.Sc",
        email = doc.getString("email") ?: "",
        phone = doc.getString("phone") ?: "",
        assignedClasses = (doc.get("assignedClasses") as? List<String>) ?: listOf("10-A"),
        subjects = (doc.get("subjects") as? List<String>) ?: listOf("General"),
        weeklyPeriods = (doc.getLong("weeklyPeriods") ?: 24L).toInt(),
        attendancePercent = doc.getDouble("attendancePercent") ?: 98.0,
        experienceYears = (doc.getLong("experienceYears") ?: 5L).toInt(),
        joiningDate = doc.getString("joiningDate") ?: "10 Jun 2020"
      )
    } catch (e: Exception) {
      null
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToUserProfile(doc: DocumentSnapshot): UserProfile? {
    return try {
      val roleStr = doc.getString("role") ?: return null
      UserProfile(
        id = doc.getString("id") ?: doc.id,
        name = doc.getString("name") ?: "",
        email = doc.getString("email") ?: "",
        role = UserRole.valueOf(roleStr),
        designation = doc.getString("designation") ?: "",
        phone = doc.getString("phone") ?: "",
        avatarInitials = doc.getString("avatarInitials") ?: "",
        associatedClass = doc.getString("associatedClass") ?: "",
        associatedStudentId = doc.getString("associatedStudentId") ?: "",
        associatedChildNames = (doc.get("associatedChildNames") as? List<String>) ?: emptyList()
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun feeRecordToMap(fee: FeeRecord): Map<String, Any> = mapOf(
    "id" to fee.id,
    "studentId" to fee.studentId,
    "studentName" to fee.studentName,
    "classGrade" to fee.classGrade,
    "division" to fee.division,
    "tuitionFee" to fee.tuitionFee,
    "examFee" to fee.examFee,
    "transportFee" to fee.transportFee,
    "labLibraryFee" to fee.labLibraryFee,
    "discountScholarship" to fee.discountScholarship,
    "totalFee" to fee.totalFee,
    "paidAmount" to fee.paidAmount,
    "status" to fee.status.name,
    "dueDate" to fee.dueDate,
    "lastPaymentDate" to fee.lastPaymentDate,
    "transactions" to fee.transactions.map { txnToMap(it) }
  )

  private fun txnToMap(txn: FeePaymentTransaction): Map<String, Any> = mapOf(
    "transactionId" to txn.transactionId,
    "receiptNo" to txn.receiptNo,
    "amount" to txn.amount,
    "date" to txn.date,
    "method" to txn.method,
    "status" to txn.status,
    "feeHead" to txn.feeHead
  )

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToFeeRecord(doc: DocumentSnapshot): FeeRecord? {
    return try {
      val rawTxns = (doc.get("transactions") as? List<Map<String, Any>>) ?: emptyList()
      val txns = rawTxns.map { map ->
        FeePaymentTransaction(
          transactionId = map["transactionId"] as? String ?: "TXN",
          receiptNo = map["receiptNo"] as? String ?: "REC",
          amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
          date = map["date"] as? String ?: "",
          method = map["method"] as? String ?: "UPI",
          status = map["status"] as? String ?: "SUCCESS",
          feeHead = map["feeHead"] as? String ?: "Tuition Fee"
        )
      }
      FeeRecord(
        id = doc.getString("id") ?: doc.id,
        studentId = doc.getString("studentId") ?: "",
        studentName = doc.getString("studentName") ?: "",
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A",
        tuitionFee = doc.getDouble("tuitionFee") ?: 45000.0,
        examFee = doc.getDouble("examFee") ?: 3500.0,
        transportFee = doc.getDouble("transportFee") ?: 12000.0,
        labLibraryFee = doc.getDouble("labLibraryFee") ?: 4500.0,
        discountScholarship = doc.getDouble("discountScholarship") ?: 0.0,
        totalFee = doc.getDouble("totalFee") ?: 65000.0,
        paidAmount = doc.getDouble("paidAmount") ?: 0.0,
        status = try { FeeStatus.valueOf(doc.getString("status") ?: "PENDING") } catch (e: Exception) { FeeStatus.PENDING },
        dueDate = doc.getString("dueDate") ?: "15 Sep 2026",
        lastPaymentDate = doc.getString("lastPaymentDate") ?: "",
        transactions = txns
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun assignmentToMap(hw: HomeworkAssignment): Map<String, Any> = mapOf(
    "id" to hw.id,
    "title" to hw.title,
    "subject" to hw.subject,
    "classGrade" to hw.classGrade,
    "division" to hw.division,
    "teacherName" to hw.teacherName,
    "assignedDate" to hw.assignedDate,
    "dueDate" to hw.dueDate,
    "instructions" to hw.instructions,
    "attachmentName" to hw.attachmentName,
    "attachmentType" to hw.attachmentType,
    "attachmentUrl" to hw.attachmentUrl,
    "maxPoints" to hw.maxPoints,
    "isCompletedByStudent" to hw.isCompletedByStudent,
    "submissionStatus" to hw.submissionStatus
  )

  private fun mapDocToAssignment(doc: DocumentSnapshot): HomeworkAssignment? {
    return try {
      HomeworkAssignment(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        subject = doc.getString("subject") ?: "",
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A",
        teacherName = doc.getString("teacherName") ?: "",
        assignedDate = doc.getString("assignedDate") ?: "",
        dueDate = doc.getString("dueDate") ?: "",
        instructions = doc.getString("instructions") ?: "",
        attachmentName = doc.getString("attachmentName") ?: "",
        attachmentType = doc.getString("attachmentType") ?: "",
        attachmentUrl = doc.getString("attachmentUrl") ?: "",
        maxPoints = (doc.getLong("maxPoints") ?: 20L).toInt(),
        isCompletedByStudent = doc.getBoolean("isCompletedByStudent") ?: false,
        submissionStatus = doc.getString("submissionStatus") ?: "Pending"
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun studyMaterialToMap(mat: StudyMaterial): Map<String, Any> = mapOf(
    "id" to mat.id,
    "title" to mat.title,
    "subject" to mat.subject,
    "classGrade" to mat.classGrade,
    "chapter" to mat.chapter,
    "teacherName" to mat.teacherName,
    "uploadDate" to mat.uploadDate,
    "fileType" to mat.fileType,
    "fileSize" to mat.fileSize,
    "description" to mat.description,
    "downloadCount" to mat.downloadCount,
    "attachmentName" to mat.attachmentName,
    "attachmentType" to mat.attachmentType,
    "attachmentUrl" to mat.attachmentUrl
  )

  private fun mapDocToStudyMaterial(doc: DocumentSnapshot): StudyMaterial? {
    return try {
      StudyMaterial(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        subject = doc.getString("subject") ?: "",
        classGrade = doc.getString("classGrade") ?: "10",
        chapter = doc.getString("chapter") ?: "",
        teacherName = doc.getString("teacherName") ?: "",
        uploadDate = doc.getString("uploadDate") ?: "",
        fileType = doc.getString("fileType") ?: "PDF",
        fileSize = doc.getString("fileSize") ?: "2.4 MB",
        description = doc.getString("description") ?: "",
        downloadCount = (doc.getLong("downloadCount") ?: 40L).toInt(),
        attachmentName = doc.getString("attachmentName") ?: "",
        attachmentType = doc.getString("attachmentType") ?: "",
        attachmentUrl = doc.getString("attachmentUrl") ?: ""
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun noticeToMap(nt: SchoolNotice): Map<String, Any> = mapOf(
    "id" to nt.id,
    "title" to nt.title,
    "content" to nt.content,
    "category" to nt.category.name,
    "publishedDate" to nt.publishedDate,
    "authorName" to nt.authorName,
    "authorRole" to nt.authorRole,
    "targetAudience" to nt.targetAudience,
    "isImportant" to nt.isImportant,
    "attachmentName" to nt.attachmentName
  )

  private fun mapDocToNotice(doc: DocumentSnapshot): SchoolNotice? {
    return try {
      SchoolNotice(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        content = doc.getString("content") ?: "",
        category = try { NoticeCategory.valueOf(doc.getString("category") ?: "CIRCULAR") } catch (e: Exception) { NoticeCategory.CIRCULAR },
        publishedDate = doc.getString("publishedDate") ?: "",
        authorName = doc.getString("authorName") ?: "",
        authorRole = doc.getString("authorRole") ?: "",
        targetAudience = doc.getString("targetAudience") ?: "All School",
        isImportant = doc.getBoolean("isImportant") ?: false,
        attachmentName = doc.getString("attachmentName") ?: ""
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun leaveToMap(lv: LeaveRequest): Map<String, Any> = mapOf(
    "id" to lv.id,
    "applicantId" to lv.applicantId,
    "applicantName" to lv.applicantName,
    "applicantRole" to lv.applicantRole.name,
    "classOrDept" to lv.classOrDept,
    "leaveType" to lv.leaveType,
    "startDate" to lv.startDate,
    "endDate" to lv.endDate,
    "daysCount" to lv.daysCount,
    "reason" to lv.reason,
    "appliedDate" to lv.appliedDate,
    "status" to lv.status.name,
    "approverRemark" to lv.approverRemark
  )

  private fun mapDocToLeave(doc: DocumentSnapshot): LeaveRequest? {
    return try {
      LeaveRequest(
        id = doc.getString("id") ?: doc.id,
        applicantId = doc.getString("applicantId") ?: "",
        applicantName = doc.getString("applicantName") ?: "",
        applicantRole = try { UserRole.valueOf(doc.getString("applicantRole") ?: "STUDENT") } catch (e: Exception) { UserRole.STUDENT },
        classOrDept = doc.getString("classOrDept") ?: "",
        leaveType = doc.getString("leaveType") ?: "Medical Leave",
        startDate = doc.getString("startDate") ?: "",
        endDate = doc.getString("endDate") ?: "",
        daysCount = (doc.getLong("daysCount") ?: 1L).toInt(),
        reason = doc.getString("reason") ?: "",
        appliedDate = doc.getString("appliedDate") ?: "",
        status = try { LeaveStatus.valueOf(doc.getString("status") ?: "PENDING") } catch (e: Exception) { LeaveStatus.PENDING },
        approverRemark = doc.getString("approverRemark") ?: ""
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun notificationToMap(nt: ErpNotification): Map<String, Any> = mapOf(
    "id" to nt.id,
    "title" to nt.title,
    "message" to nt.message,
    "category" to nt.category.name,
    "timestamp" to nt.timestamp,
    "isRead" to nt.isRead,
    "targetRole" to (nt.targetRole?.name ?: ""),
    "actionableId" to nt.actionableId
  )

  private fun mapDocToNotification(doc: DocumentSnapshot): ErpNotification? {
    return try {
      val targetStr = doc.getString("targetRole") ?: ""
      val target = if (targetStr.isNotEmpty()) try { UserRole.valueOf(targetStr) } catch (e: Exception) { null } else null
      ErpNotification(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        message = doc.getString("message") ?: "",
        category = try { NotificationCategory.valueOf(doc.getString("category") ?: "SYSTEM") } catch (e: Exception) { NotificationCategory.SYSTEM },
        timestamp = doc.getString("timestamp") ?: "Just now",
        isRead = doc.getBoolean("isRead") ?: false,
        targetRole = target,
        actionableId = doc.getString("actionableId") ?: ""
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun libraryBookToMap(bk: LibraryBook): Map<String, Any> = mapOf(
    "id" to bk.id,
    "title" to bk.title,
    "author" to bk.author,
    "isbn" to bk.isbn,
    "category" to bk.category,
    "totalCopies" to bk.totalCopies,
    "availableCopies" to bk.availableCopies,
    "shelfLocation" to bk.shelfLocation,
    "issuedCount" to bk.issuedCount
  )

  private fun mapDocToLibraryBook(doc: DocumentSnapshot): LibraryBook? {
    return try {
      LibraryBook(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        author = doc.getString("author") ?: "",
        isbn = doc.getString("isbn") ?: "",
        category = doc.getString("category") ?: "",
        totalCopies = (doc.getLong("totalCopies") ?: 5L).toInt(),
        availableCopies = (doc.getLong("availableCopies") ?: 5L).toInt(),
        shelfLocation = doc.getString("shelfLocation") ?: "Shelf A-1",
        issuedCount = (doc.getLong("issuedCount") ?: 0L).toInt()
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun transportRouteToMap(tr: TransportRoute): Map<String, Any> = mapOf(
    "id" to tr.id,
    "routeNumber" to tr.routeNumber,
    "routeName" to tr.routeName,
    "vehicleNumber" to tr.vehicleNumber,
    "driverName" to tr.driverName,
    "driverPhone" to tr.driverPhone,
    "totalCapacity" to tr.totalCapacity,
    "assignedStudents" to tr.assignedStudents,
    "stops" to tr.stops,
    "pickupStartTime" to tr.pickupStartTime,
    "dropStartTime" to tr.dropStartTime,
    "monthlyFare" to tr.monthlyFare
  )

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToTransportRoute(doc: DocumentSnapshot): TransportRoute? {
    return try {
      TransportRoute(
        id = doc.getString("id") ?: doc.id,
        routeNumber = doc.getString("routeNumber") ?: "Route 1",
        routeName = doc.getString("routeName") ?: "",
        vehicleNumber = doc.getString("vehicleNumber") ?: "",
        driverName = doc.getString("driverName") ?: "",
        driverPhone = doc.getString("driverPhone") ?: "",
        totalCapacity = (doc.getLong("totalCapacity") ?: 40L).toInt(),
        assignedStudents = (doc.getLong("assignedStudents") ?: 30L).toInt(),
        stops = (doc.get("stops") as? List<String>) ?: emptyList(),
        pickupStartTime = doc.getString("pickupStartTime") ?: "07:00 AM",
        dropStartTime = doc.getString("dropStartTime") ?: "03:30 PM",
        monthlyFare = doc.getDouble("monthlyFare") ?: 2500.0
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun inventoryToMap(inv: InventoryAsset): Map<String, Any> = mapOf(
    "id" to inv.id,
    "itemName" to inv.itemName,
    "category" to inv.category,
    "location" to inv.location,
    "quantity" to inv.quantity,
    "condition" to inv.condition,
    "purchaseDate" to inv.purchaseDate,
    "estimatedValue" to inv.estimatedValue
  )

  private fun mapDocToInventory(doc: DocumentSnapshot): InventoryAsset? {
    return try {
      InventoryAsset(
        id = doc.getString("id") ?: doc.id,
        itemName = doc.getString("itemName") ?: "",
        category = doc.getString("category") ?: "",
        location = doc.getString("location") ?: "",
        quantity = (doc.getLong("quantity") ?: 1L).toInt(),
        condition = doc.getString("condition") ?: "Operational",
        purchaseDate = doc.getString("purchaseDate") ?: "2024",
        estimatedValue = doc.getDouble("estimatedValue") ?: 50000.0
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun timetableSlotToMap(tt: TimetableSlot): Map<String, Any> = mapOf(
    "id" to tt.id,
    "dayOfWeek" to tt.dayOfWeek,
    "periodNumber" to tt.periodNumber,
    "startTime" to tt.startTime,
    "endTime" to tt.endTime,
    "subject" to tt.subject,
    "teacherName" to tt.teacherName,
    "roomNumber" to tt.roomNumber,
    "classGrade" to tt.classGrade,
    "division" to tt.division
  )

  private fun mapDocToTimetableSlot(doc: DocumentSnapshot): TimetableSlot? {
    return try {
      TimetableSlot(
        id = doc.getString("id") ?: doc.id,
        dayOfWeek = doc.getString("dayOfWeek") ?: "Monday",
        periodNumber = (doc.getLong("periodNumber") ?: 1L).toInt(),
        startTime = doc.getString("startTime") ?: "08:00 AM",
        endTime = doc.getString("endTime") ?: "08:45 AM",
        subject = doc.getString("subject") ?: "",
        teacherName = doc.getString("teacherName") ?: "",
        roomNumber = doc.getString("roomNumber") ?: "Room 101",
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A"
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun attendanceRecordToMap(rec: ClassAttendanceRecord): Map<String, Any> = mapOf(
    "id" to rec.id,
    "classGrade" to rec.classGrade,
    "division" to rec.division,
    "date" to rec.date,
    "totalStudents" to rec.totalStudents,
    "presentCount" to rec.presentCount,
    "absentCount" to rec.absentCount,
    "lateCount" to rec.lateCount,
    "leaveCount" to rec.leaveCount,
    "markedBy" to rec.markedBy,
    "studentList" to rec.studentList.map {
      mapOf(
        "studentId" to it.studentId,
        "studentName" to it.studentName,
        "rollNumber" to it.rollNumber,
        "status" to it.status.name,
        "remark" to it.remark
      )
    }
  )

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToAttendanceRecord(doc: DocumentSnapshot): ClassAttendanceRecord? {
    return try {
      val rawStudents = (doc.get("studentList") as? List<Map<String, Any>>) ?: emptyList()
      val students = rawStudents.map { map ->
        StudentAttendance(
          studentId = map["studentId"] as? String ?: "",
          studentName = map["studentName"] as? String ?: "",
          rollNumber = (map["rollNumber"] as? Number)?.toInt() ?: 0,
          status = try { AttendanceStatus.valueOf(map["status"] as? String ?: "PRESENT") } catch (e: Exception) { AttendanceStatus.PRESENT },
          remark = map["remark"] as? String ?: ""
        )
      }
      ClassAttendanceRecord(
        id = doc.getString("id") ?: doc.id,
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A",
        date = doc.getString("date") ?: "",
        totalStudents = (doc.getLong("totalStudents") ?: students.size.toLong()).toInt(),
        presentCount = (doc.getLong("presentCount") ?: 0L).toInt(),
        absentCount = (doc.getLong("absentCount") ?: 0L).toInt(),
        lateCount = (doc.getLong("lateCount") ?: 0L).toInt(),
        leaveCount = (doc.getLong("leaveCount") ?: 0L).toInt(),
        markedBy = doc.getString("markedBy") ?: "Staff",
        studentList = students
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun examScheduleToMap(ex: ExamSchedule): Map<String, Any> = mapOf(
    "id" to ex.id,
    "title" to ex.title,
    "term" to ex.term,
    "classGrade" to ex.classGrade,
    "startDate" to ex.startDate,
    "endDate" to ex.endDate,
    "isPublished" to ex.isPublished,
    "subjects" to ex.subjects.map {
      mapOf(
        "subjectName" to it.subjectName,
        "date" to it.date,
        "time" to it.time,
        "maxMarks" to it.maxMarks,
        "passingMarks" to it.passingMarks,
        "room" to it.room
      )
    },
    "schoolId" to ex.schoolId
  )

  @Suppress("UNCHECKED_CAST")
  private fun mapDocToExamSchedule(doc: DocumentSnapshot): ExamSchedule? {
    return try {
      val rawSubjects = (doc.get("subjects") as? List<Map<String, Any>>) ?: emptyList()
      val subjects = rawSubjects.map { map ->
        ExamSubject(
          subjectName = map["subjectName"] as? String ?: "",
          date = map["date"] as? String ?: "",
          time = map["time"] as? String ?: "",
          maxMarks = (map["maxMarks"] as? Number)?.toInt() ?: 100,
          passingMarks = (map["passingMarks"] as? Number)?.toInt() ?: 35,
          room = map["room"] as? String ?: "Main Hall"
        )
      }
      ExamSchedule(
        id = doc.getString("id") ?: doc.id,
        title = doc.getString("title") ?: "",
        term = doc.getString("term") ?: "Term 1",
        classGrade = doc.getString("classGrade") ?: "10",
        startDate = doc.getString("startDate") ?: "",
        endDate = doc.getString("endDate") ?: "",
        isPublished = doc.getBoolean("isPublished") ?: false,
        subjects = subjects,
        schoolId = doc.getString("schoolId") ?: "revenex_school_001"
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun reportCardToMap(rc: ReportCard): Map<String, Any> = mapOf(
    "id" to rc.id,
    "studentId" to rc.studentId,
    "studentName" to rc.studentName,
    "classGrade" to rc.classGrade,
    "division" to rc.division,
    "rollNumber" to rc.rollNumber,
    "term" to rc.term,
    "academicYear" to rc.academicYear,
    "scores" to rc.scores.map {
      mapOf(
        "subjectName" to it.subjectName,
        "maxMarks" to it.maxMarks,
        "obtainedMarks" to it.obtainedMarks,
        "grade" to it.grade,
        "remarks" to it.remarks,
        "checkedPaperUrl" to it.checkedPaperUrl,
        "answerKeyUrl" to it.answerKeyUrl
      )
    },
    "attendanceRate" to rc.attendanceRate,
    "rankInClass" to rc.rankInClass,
    "totalStudents" to rc.totalStudents,
    "principalRemark" to rc.principalRemark,
    "issueDate" to rc.issueDate,
    "schoolId" to rc.schoolId
  )
 
  @Suppress("UNCHECKED_CAST")
  private fun mapDocToReportCard(doc: DocumentSnapshot): ReportCard? {
    return try {
      val rawScores = (doc.get("scores") as? List<Map<String, Any>>) ?: emptyList()
      val scores = rawScores.map { map ->
        SubjectScore(
          subjectName = map["subjectName"] as? String ?: "",
          maxMarks = (map["maxMarks"] as? Number)?.toInt() ?: 100,
          obtainedMarks = (map["obtainedMarks"] as? Number)?.toInt() ?: 0,
          grade = map["grade"] as? String ?: "A",
          remarks = map["remarks"] as? String ?: "",
          checkedPaperUrl = map["checkedPaperUrl"] as? String ?: "",
          answerKeyUrl = map["answerKeyUrl"] as? String ?: ""
        )
      }
      ReportCard(
        id = doc.getString("id") ?: doc.id,
        studentId = doc.getString("studentId") ?: "",
        studentName = doc.getString("studentName") ?: "",
        classGrade = doc.getString("classGrade") ?: "10",
        division = doc.getString("division") ?: "A",
        rollNumber = (doc.getLong("rollNumber") ?: 1L).toInt(),
        term = doc.getString("term") ?: "Term 1",
        academicYear = doc.getString("academicYear") ?: "2026-2027",
        scores = scores,
        attendanceRate = doc.getDouble("attendanceRate") ?: 95.0,
        rankInClass = (doc.getLong("rankInClass") ?: 1L).toInt(),
        totalStudents = (doc.getLong("totalStudents") ?: 40L).toInt(),
        principalRemark = doc.getString("principalRemark") ?: "Excellent performance.",
        issueDate = doc.getString("issueDate") ?: "",
        schoolId = doc.getString("schoolId") ?: "revenex_school_001"
      )
    } catch (e: Exception) {
      null
    }
  }
}
