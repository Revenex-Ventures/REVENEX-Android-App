package com.example.data.datasource

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * ERP Data Source abstraction contract.
 * Enables interchangeable local demo data and live cloud Firestore persistence.
 */
interface ErpDataSource {
  // Configuration & Mode
  fun isProductionMode(): Boolean
  fun getBackendCapabilitiesFlow():Flow<SchoolBackendCapabilities> = kotlinx.coroutines.flow.flowOf(SchoolBackendCapabilities(loaded=true,assignments=true))

  // Authentication & Current User
  fun getCurrentUserFlow(): Flow<UserProfile>
  suspend fun setCurrentUser(user: UserProfile)
  suspend fun switchRole(role: UserRole)
  suspend fun login(email: String, role: UserRole, name: String)
  suspend fun logout()

  // Students
  fun getStudentsFlow(): Flow<List<Student>>
  suspend fun addStudent(student: Student)
  suspend fun updateStudent(student: Student)
  suspend fun deleteStudent(studentId: String)

  // Teachers / Faculty
  fun getTeachersFlow(): Flow<List<Teacher>>
  suspend fun addTeacher(teacher: Teacher)
  suspend fun updateTeacher(teacher: Teacher)

  // Attendance
  fun getClassAttendanceFlow(): Flow<Map<String, ClassAttendanceRecord>>
  suspend fun saveClassAttendance(
    classGrade: String,
    division: String,
    date: String,
    markedBy: String,
    studentList: List<StudentAttendance>
  )

  // Fees & Accounts
  fun getFeeRecordsFlow(): Flow<List<FeeRecord>>
  suspend fun processFeePayment(
    studentId: String,
    amountPaid: Long,
    paymentMethod: String,
    feeHead: String,
    razorpayPaymentId: String? = null,
    razorpayOrderId: String? = null,
    razorpaySignature: String? = null
  ): FeePaymentTransaction

  // Leave Management
  suspend fun requestLeave(id:String,type:String,start:String,end:String,reason:String):Unit = error("Confirmed leave requests are unavailable for this data source.")
  fun getLeaveRequestsFlow(): Flow<List<LeaveRequest>>
  suspend fun applyLeave(
    applicantName: String,
    applicantRole: UserRole,
    applicantId: String,
    classOrDept: String,
    leaveType: String,
    startDate: String,
    endDate: String,
    daysCount: Int,
    reason: String
  )
  suspend fun updateLeaveStatus(leaveId: String, status: LeaveStatus, approverRemark: String)

  // Academics (Homework, Study Material, Report Cards, Timetable)
  fun getAssignmentsFlow(): Flow<List<HomeworkAssignment>>
  suspend fun createAssignment(assignment: HomeworkAssignment)
  suspend fun toggleAssignmentCompletion(assignmentId: String)
  fun getAssignmentSubmissionsFlow():Flow<List<AssignmentSubmission>> = kotlinx.coroutines.flow.flowOf(emptyList())
  suspend fun submitAssignment(assignmentId:String,attachmentRef:String,note:String,expectedVersion:Int):AssignmentSubmission =
    error("File and note submissions require the configured school backend.")
  suspend fun reviewSubmission(id:String,feedback:String,expectedVersion:Int):AssignmentSubmission =
    error("Submission review requires the configured school backend.")

  fun getStudyMaterialsFlow(): Flow<List<StudyMaterial>>
  suspend fun uploadStudyMaterial(material: StudyMaterial)

  fun getReportCardsFlow(): Flow<List<ReportCard>>
  suspend fun saveStudentMarks(studentId: String, term: String, subjectName: String, obtainedMarks: Int, maxMarks: Int)
  suspend fun saveDraftMarks(studentId:String,term:String,subject:String,obtained:Int,maximum:Int,expectedVersion:Int):Int {
    saveStudentMarks(studentId,term,subject,obtained,maximum)
    return 0 // Local implementations do not claim a database version.
  }
  suspend fun submitMarksForReview(studentId:String,term:String,subject:String,expectedVersion:Int):Int =
    error("Mark review requires the configured school backend.")
  suspend fun saveCheckedPaper(studentId: String, term: String, subjectName: String, checkedPaperUrl: String)
  suspend fun saveAnswerKey(classGrade: String, term: String, subjectName: String, answerKeyUrl: String)
  fun getExamSchedulesFlow(): Flow<List<ExamSchedule>>
  fun getTimetableFlow(): Flow<List<TimetableSlot>>
  fun getEventsFlow(): Flow<List<SchoolEvent>>

  // Communication (Notices & Notifications)
  fun getNoticesFlow(): Flow<List<SchoolNotice>>
  suspend fun publishNotice(notice: SchoolNotice)

  fun getNotificationsFlow(): Flow<List<ErpNotification>>
  suspend fun addNotification(
    title: String,
    message: String,
    category: NotificationCategory,
    targetRole: UserRole? = null
  )
  suspend fun markNotificationAsRead(id: String)
  suspend fun markAllNotificationsAsRead()

  // School Operations
  fun getTransportRoutesFlow(): Flow<List<TransportRoute>>
  fun getLibraryBooksFlow(): Flow<List<LibraryBook>>
  suspend fun issueBook(bookId: String)
  suspend fun returnBook(bookId: String)
  suspend fun publishReportCard(studentId: String, term: String, published: Boolean)

  fun getInventoryFlow(): Flow<List<InventoryAsset>>
}
