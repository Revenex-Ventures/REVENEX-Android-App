package com.example.data.datasource

import com.example.data.local.StudentProfileStore
import com.example.data.local.TeacherProfileStore
import com.example.data.model.*
import com.example.data.repository.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Robust in-memory implementation of [ErpDataSource] with realistic Indian school sample dataset.
 * Guarantees instantaneous cross-role synchronization for Demo & Offline testing.
 */
class LocalDemoDataSource : ErpDataSource {

  private val _currentUser = MutableStateFlow(SampleData.defaultProfiles[0])
  private val _students = MutableStateFlow(StudentProfileStore.applyOverrides(SampleData.initialStudents))
  private val _teachers = MutableStateFlow(TeacherProfileStore.applyOverrides(SampleData.initialTeachers))
  private val _feeRecords = MutableStateFlow(SampleData.initialFeeRecords)
  private val _assignments = MutableStateFlow(SampleData.initialAssignments)
  private val _studyMaterials = MutableStateFlow(SampleData.initialStudyMaterials)
  private val _notices = MutableStateFlow(SampleData.initialNotices)
  private val _leaveRequests = MutableStateFlow(SampleData.initialLeaveRequests)
  private val _reportCards = MutableStateFlow(SampleData.initialReportCards)
  private val _examSchedules = MutableStateFlow(SampleData.initialExamSchedules)
  private val _timetable = MutableStateFlow(SampleData.initialTimetable)
  private val _events = MutableStateFlow(SampleData.initialEvents)
  private val _transportRoutes = MutableStateFlow(SampleData.initialTransportRoutes)
  private val _libraryBooks = MutableStateFlow(SampleData.initialLibraryBooks)
  private val _inventory = MutableStateFlow(SampleData.initialInventory)
  private val _notifications = MutableStateFlow(SampleData.initialNotifications)

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

  override fun isProductionMode(): Boolean = false

  override fun getCurrentUserFlow(): Flow<UserProfile> = _currentUser.asStateFlow()

  override suspend fun setCurrentUser(user: UserProfile) {
    _currentUser.value = user
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
    _currentUser.value = targetProfile
  }

  override suspend fun login(email: String, role: UserRole, name: String) {
    val initials = name.split(" ")
      .mapNotNull { it.firstOrNull()?.toString() }
      .joinToString("")
      .take(2)
      .uppercase()

    // For student login, find actual student record to get correct associatedStudentId
    val matchedStudentId = if (role == UserRole.STUDENT || role == UserRole.PARENT) {
      _students.value.firstOrNull {
        it.parentEmail.equals(email, ignoreCase = true) ||
        it.name.equals(name, ignoreCase = true)
      }?.id ?: "stu_1001"
    } else ""

    _currentUser.value = UserProfile(
      id = "user_${System.currentTimeMillis()}",
      name = name.ifBlank { role.displayName },
      email = email,
      role = role,
      designation = role.displayName,
      phone = "+91 98220 12345",
      avatarInitials = if (initials.isNotEmpty()) initials else "RX",
      associatedClass = if (role == UserRole.TEACHER || role == UserRole.STUDENT) "10-A" else "",
      associatedStudentId = matchedStudentId,
      associatedChildNames = if (role == UserRole.PARENT) listOf("Aarav Patel (10-A)", "Diya Patel (6-B)") else emptyList()
    )
  }

  override suspend fun logout() {
    _currentUser.value = SampleData.defaultProfiles[0]
  }

  // Students
  override fun getStudentsFlow(): Flow<List<Student>> = _students.asStateFlow()

  override suspend fun addStudent(student: Student) {
    _students.update { listOf(student) + it }
    val fee = FeeRecord(
      id = "fee_${student.id}",
      studentId = student.id,
      studentName = student.name,
      classGrade = student.classGrade,
      division = student.division,
      tuitionFee = 4500000L,
      examFee = 350000L,
      transportFee = 1200000L,
      labLibraryFee = 450000L,
      discountScholarship = 0L,
      totalFee = 6500000L,
      paidAmount = (6500000L - student.feePendingAmount).coerceAtLeast(0L),
      status = student.feeStatus,
      dueDate = "15 Sep 2026"
    )
    _feeRecords.update { listOf(fee) + it }
    addNotification(
      title = "New Student Enrolled",
      message = "${student.name} admitted to Class ${student.fullClass} (Adm #${student.admissionNumber}).",
      category = NotificationCategory.SYSTEM,
      targetRole = UserRole.PRINCIPAL
    )
  }

  override suspend fun updateStudent(student: Student) {
    StudentProfileStore.save(student)
    _students.update { list ->
      list.map { if (it.id == student.id) student else it }
    }
  }

  override suspend fun deleteStudent(studentId: String) {
    StudentProfileStore.remove(studentId)
    _students.update { list -> list.filterNot { it.id == studentId } }
    _feeRecords.update { list -> list.filterNot { it.studentId == studentId } }
  }

  // Teachers
  override fun getTeachersFlow(): Flow<List<Teacher>> = _teachers.asStateFlow()

  override suspend fun addTeacher(teacher: Teacher) {
    TeacherProfileStore.save(teacher)
    _teachers.update { listOf(teacher) + it }
    addNotification(
      title = "Faculty Profile Added",
      message = "${teacher.name} appointed as ${teacher.designation}.",
      category = NotificationCategory.SYSTEM,
      targetRole = UserRole.PRINCIPAL
    )
  }

  override suspend fun updateTeacher(teacher: Teacher) {
    TeacherProfileStore.save(teacher)
    _teachers.update { list ->
      list.map { if (it.id == teacher.id) teacher else it }
    }
  }

  // Attendance
  override fun getClassAttendanceFlow(): Flow<Map<String, ClassAttendanceRecord>> = _classAttendance.asStateFlow()

  override suspend fun saveClassAttendance(
    classGrade: String,
    division: String,
    date: String,
    markedBy: String,
    studentList: List<StudentAttendance>
  ) {
    val total = studentList.size
    val present = studentList.count { it.status == AttendanceStatus.PRESENT }
    val absent = studentList.count { it.status == AttendanceStatus.ABSENT }
    val late = studentList.count { it.status == AttendanceStatus.LATE }
    val leave = studentList.count { it.status == AttendanceStatus.LEAVE }

    val record = ClassAttendanceRecord(
      id = "att_${classGrade}${division}_${System.currentTimeMillis()}",
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

    // Date-stamped key preserves attendance history across days. Re-submitting for
    // the same class+division+date reuses the same key (no duplicate), while a new
    // date creates a distinct record instead of overwriting the previous day's.
    val key = "${classGrade}-${division}_${date}"
    _classAttendance.update { it + (key to record) }

    // Synchronize individual students' attendance percentage based on actual records
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
          st.copy(attendancePercent = String.format(Locale.US, "%.1f", rate).toDouble())
        } else {
          st
        }
      }
    }

    addNotification(
      title = "Attendance Saved: Class $classGrade-$division",
      message = "Marked by $markedBy. Present: $present, Absent: $absent, Late: $late.",
      category = NotificationCategory.ATTENDANCE
    )
  }

  // Fees & Accounts
  override fun getFeeRecordsFlow(): Flow<List<FeeRecord>> = _feeRecords.asStateFlow()

  override suspend fun processFeePayment(
    studentId: String,
    amountPaid: Long,
    paymentMethod: String,
    feeHead: String,
    razorpayPaymentId: String?,
    razorpayOrderId: String?,
    razorpaySignature: String?
  ): FeePaymentTransaction {
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

    _feeRecords.update { list ->
      list.map { record ->
        if (record.studentId == studentId) {
          val newPaid = record.paidAmount + correctAmount
          val newStatus = if (newPaid >= record.totalFee) FeeStatus.PAID else FeeStatus.PARTIAL
          record.copy(
            paidAmount = newPaid,
            status = newStatus,
            lastPaymentDate = getCurrentFormattedDate(),
            transactions = listOf(txn) + record.transactions
          )
        } else record
      }
    }

    _students.update { list ->
      list.map { st ->
        if (st.id == studentId) {
          val currentPending = st.feePendingAmount - correctAmount
          val newPending = currentPending.coerceAtLeast(0L)
          val newStatus = if (newPending == 0L) FeeStatus.PAID else FeeStatus.PARTIAL
          st.copy(feePendingAmount = newPending, feeStatus = newStatus)
        } else st
      }
    }

    addNotification(
      title = "Fee Payment Successful (₹${amountPaid / 100})",
      message = "Receipt #${txn.receiptNo} generated via ${txn.method}. Account updated.",
      category = NotificationCategory.FEES
    )

    return txn
  }

  // Leave Management
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
    addNotification(
      title = "New Leave Application",
      message = "$applicantName ($classOrDept) applied for $daysCount day(s) $leaveType.",
      category = NotificationCategory.LEAVE,
      targetRole = UserRole.PRINCIPAL
    )
  }

  override suspend fun updateLeaveStatus(leaveId: String, status: LeaveStatus, approverRemark: String) {
    _leaveRequests.update { list ->
      list.map {
        if (it.id == leaveId) it.copy(status = status, approverRemark = approverRemark)
        else it
      }
    }
    val statusText = if (status == LeaveStatus.APPROVED) "Approved" else "Rejected"
    addNotification(
      title = "Leave Request $statusText",
      message = "Leave application has been $statusText. Remark: $approverRemark",
      category = NotificationCategory.LEAVE
    )
  }

  // Academics
  override fun getAssignmentsFlow(): Flow<List<HomeworkAssignment>> = _assignments.asStateFlow()

  override suspend fun createAssignment(assignment: HomeworkAssignment) {
    _assignments.update { listOf(assignment) + it }
    addNotification(
      title = "New Assignment: ${assignment.subject}",
      message = "${assignment.title} due on ${assignment.dueDate}.",
      category = NotificationCategory.HOMEWORK,
      targetRole = UserRole.STUDENT
    )
  }

  override suspend fun toggleAssignmentCompletion(assignmentId: String) {
    _assignments.update { list ->
      list.map {
        if (it.id == assignmentId) {
          val next = !it.isCompletedByStudent
          it.copy(
            isCompletedByStudent = next,
            submissionStatus = if (next) "Submitted" else "Pending"
          )
        } else it
      }
    }
  }

  override fun getStudyMaterialsFlow(): Flow<List<StudyMaterial>> = _studyMaterials.asStateFlow()

  override suspend fun uploadStudyMaterial(material: StudyMaterial) {
    _studyMaterials.update { listOf(material) + it }
    addNotification(
      title = "New Study Resource Uploaded",
      message = "${material.subject}: ${material.title} (${material.chapter}).",
      category = NotificationCategory.ANNOUNCEMENTS,
      targetRole = UserRole.STUDENT
    )
  }

  override fun getReportCardsFlow(): Flow<List<ReportCard>> = _reportCards.asStateFlow()

  override suspend fun saveStudentMarks(
    studentId: String,
    term: String,
    subjectName: String,
    obtainedMarks: Int,
    maxMarks: Int
  ) {
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
          card.copy(scores = updatedScores)
        } else card
      }
    }
  }

  override suspend fun saveCheckedPaper(
    studentId: String,
    term: String,
    subjectName: String,
    checkedPaperUrl: String
  ) {
    _reportCards.update { list ->
      list.map { card ->
        if (card.studentId == studentId && card.term.contains(term, ignoreCase = true)) {
          val updatedScores = card.scores.map { score ->
            if (score.subjectName.equals(subjectName, ignoreCase = true)) {
              score.copy(checkedPaperUrl = checkedPaperUrl)
            } else score
          }
          card.copy(scores = updatedScores)
        } else card
      }
    }
  }

  override suspend fun saveAnswerKey(
    classGrade: String,
    term: String,
    subjectName: String,
    answerKeyUrl: String
  ) {
    _reportCards.update { list ->
      list.map { card ->
        if (card.classGrade.equals(classGrade, ignoreCase = true) && card.term.contains(term, ignoreCase = true)) {
          val updatedScores = card.scores.map { score ->
            if (score.subjectName.equals(subjectName, ignoreCase = true)) {
              score.copy(answerKeyUrl = answerKeyUrl)
            } else score
          }
          card.copy(scores = updatedScores)
        } else card
      }
    }
  }
  override fun getExamSchedulesFlow(): Flow<List<ExamSchedule>> = _examSchedules.asStateFlow()
  override fun getTimetableFlow(): Flow<List<TimetableSlot>> = _timetable.asStateFlow()
  override fun getEventsFlow(): Flow<List<SchoolEvent>> = _events.asStateFlow()

  // Communication
  override fun getNoticesFlow(): Flow<List<SchoolNotice>> = _notices.asStateFlow()

  override suspend fun publishNotice(notice: SchoolNotice) {
    _notices.update { listOf(notice) + it }
    addNotification(
      title = notice.title,
      message = notice.content.take(120) + "...",
      category = NotificationCategory.ANNOUNCEMENTS
    )
  }

  override fun getNotificationsFlow(): Flow<List<ErpNotification>> = _notifications.asStateFlow()

  override suspend fun addNotification(
    title: String,
    message: String,
    category: NotificationCategory,
    targetRole: UserRole?
  ) {
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
  }

  override suspend fun markNotificationAsRead(id: String) {
    _notifications.update { list ->
      list.map { if (it.id == id) it.copy(isRead = true) else it }
    }
  }

  override suspend fun markAllNotificationsAsRead() {
    _notifications.update { list -> list.map { it.copy(isRead = true) } }
  }

  // School Operations
  override fun getTransportRoutesFlow(): Flow<List<TransportRoute>> = _transportRoutes.asStateFlow()
  override fun getLibraryBooksFlow(): Flow<List<LibraryBook>> = _libraryBooks.asStateFlow()

  override suspend fun issueBook(bookId: String) {
    _libraryBooks.update { list ->
      list.map {
        if (it.id == bookId && it.availableCopies > 0) {
          it.copy(
            availableCopies = it.availableCopies - 1,
            issuedCount = it.issuedCount + 1
          )
        } else it
      }
    }
  }

  override suspend fun returnBook(bookId: String) {
    _libraryBooks.update { list ->
      list.map {
        if (it.id == bookId) {
          it.copy(
            availableCopies = (it.availableCopies + 1).coerceAtMost(it.totalCopies),
            issuedCount = (it.issuedCount - 1).coerceAtLeast(0)
          )
        } else it
      }
    }
  }

  override fun getInventoryFlow(): Flow<List<InventoryAsset>> = _inventory.asStateFlow()

  override suspend fun publishReportCard(studentId: String, term: String, published: Boolean) {
    _reportCards.update { list ->
      list.map {
        if (it.studentId == studentId && it.term == term) {
          it.copy(published = published)
        } else it
      }
    }
  }

  private fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
  }
}
