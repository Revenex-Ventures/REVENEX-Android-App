package com.example.data.model

enum class UserRole(val displayName: String) {
  PRINCIPAL("Principal / Admin"),
  TEACHER("Teacher / Faculty"),
  STUDENT("Student"),
  PARENT("Parent")
}

data class UserProfile(
  val id: String,
  val name: String,
  val email: String,
  val role: UserRole,
  val designation: String,
  val phone: String,
  val avatarInitials: String,
  val associatedClass: String = "",
  val associatedStudentId: String = "",
  val associatedChildNames: List<String> = emptyList(),
  val schoolId: String = "revenex_school_001"
)

data class Student(
  val id: String,
  val admissionNumber: String,
  val rollNumber: Int,
  val name: String,
  val classGrade: String,
  val division: String,
  val dob: String,
  val gender: String,
  val bloodGroup: String,
  val parentName: String,
  val parentPhone: String,
  val parentEmail: String,
  val address: String,
  val admissionDate: String,
  val attendancePercent: Double,
  val feeStatus: FeeStatus,
  val feePendingAmount: Long,
  val rank: Int = 1,
  val gpa: Double = 9.2,
  val avatarColorHex: Long = 0xFF2563EB,
  val schoolId: String = "revenex_school_001",
  val status: String = "ACTIVE",
  val sessionId: String = "session_2026_2027"
) {
  val fullClass: String get() = "$classGrade-$division"
}

data class Teacher(
  val id: String,
  val employeeId: String,
  val name: String,
  val dob: String,
  val gender: String = "Male",
  val department: String,
  val designation: String,
  val qualification: String,
  val email: String,
  val phone: String,
  val assignedClasses: List<String>,
  val subjects: List<String>,
  val weeklyPeriods: Int,
  val attendancePercent: Double,
  val experienceYears: Int,
  val joiningDate: String,
  val schoolId: String = "revenex_school_001",
  val status: String = "ACTIVE",
  val sessionId: String = "session_2026_2027"
)

enum class AttendanceStatus(val label: String) {
  PRESENT("Present"),
  ABSENT("Absent"),
  LATE("Late"),
  LEAVE("On Leave")
}

data class StudentAttendance(
  val studentId: String,
  val studentName: String,
  val rollNumber: Int,
  val status: AttendanceStatus,
  val remark: String = ""
)

data class ClassAttendanceRecord(
  val id: String,
  val classGrade: String,
  val division: String,
  val date: String,
  val totalStudents: Int,
  val presentCount: Int,
  val absentCount: Int,
  val lateCount: Int,
  val leaveCount: Int,
  val markedBy: String,
  val studentList: List<StudentAttendance>,
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027"
) {
  val percentage: Double
    get() = if (totalStudents > 0) ((presentCount + lateCount).toDouble() / totalStudents) * 100 else 0.0
}

enum class LeaveStatus(val label: String) {
  PENDING("Pending"),
  APPROVED("Approved"),
  REJECTED("Rejected")
}

data class LeaveRequest(
  val id: String,
  val applicantId: String,
  val applicantName: String,
  val applicantRole: UserRole,
  val classOrDept: String,
  val leaveType: String, // Medical, Casual, Family Emergency, Academic
  val startDate: String,
  val endDate: String,
  val daysCount: Int,
  val reason: String,
  val appliedDate: String,
  val status: LeaveStatus,
  val approverRemark: String = "",
  val schoolId: String = "revenex_school_001"
)

enum class FeeStatus(val label: String) {
  PAID("Paid"),
  PARTIAL("Partially Paid"),
  PENDING("Pending"),
  OVERDUE("Overdue")
}

data class FeePaymentTransaction(
  val transactionId: String,
  val receiptNo: String,
  val amount: Long,
  val date: String,
  val method: String, // UPI, Credit Card, Net Banking, Cash
  val status: String = "SUCCESS",
  val feeHead: String = "Tuition & Term Fee",
  val sessionId: String = "session_2026_2027"
)

data class FeeRecord(
  val id: String,
  val studentId: String,
  val studentName: String,
  val classGrade: String,
  val division: String,
  val tuitionFee: Long,
  val examFee: Long,
  val transportFee: Long,
  val labLibraryFee: Long,
  val discountScholarship: Long,
  val totalFee: Long,
  val paidAmount: Long,
  val status: FeeStatus,
  val dueDate: String,
  val lastPaymentDate: String = "",
  val transactions: List<FeePaymentTransaction> = emptyList(),
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027"
) {
  val pendingAmount: Long get() = (totalFee - paidAmount).coerceAtLeast(0L)
}

data class ExamSchedule(
  val id: String,
  val title: String,
  val term: String,
  val classGrade: String,
  val startDate: String,
  val endDate: String,
  val isPublished: Boolean = false,
  val subjects: List<ExamSubject>,
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027"
)

data class ExamSubject(
  val subjectName: String,
  val date: String,
  val time: String,
  val maxMarks: Int,
  val passingMarks: Int,
  val room: String
)

data class SubjectScore(
  val subjectName: String,
  val maxMarks: Int,
  val obtainedMarks: Int,
  val grade: String,
  val remarks: String,
  val checkedPaperUrl: String = "",
  val answerKeyUrl: String = ""
)

data class ReportCard(
  val id: String,
  val studentId: String,
  val studentName: String,
  val classGrade: String,
  val division: String,
  val rollNumber: Int,
  val term: String,
  val academicYear: String,
  val scores: List<SubjectScore>,
  val attendanceRate: Double,
  val rankInClass: Int,
  val totalStudents: Int,
  val principalRemark: String,
  val issueDate: String,
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027",
  val published: Boolean = false
) {
  val totalObtained: Int get() = scores.sumOf { it.obtainedMarks }
  val totalMax: Int get() = scores.sumOf { it.maxMarks }
  val percentage: Double get() = if (totalMax > 0) (totalObtained.toDouble() / totalMax) * 100 else 0.0
  val overallGrade: String get() = when {
    percentage >= 91.0 -> "A1"
    percentage >= 81.0 -> "A2"
    percentage >= 71.0 -> "B1"
    percentage >= 61.0 -> "B2"
    percentage >= 51.0 -> "C1"
    percentage >= 41.0 -> "C2"
    percentage >= 33.0 -> "D"
    else -> "E"
  }
}

data class HomeworkAssignment(
  val id: String,
  val title: String,
  val subject: String,
  val classGrade: String,
  val division: String,
  val teacherName: String,
  val assignedDate: String,
  val dueDate: String,
  val instructions: String,
  val attachmentName: String = "",
  val attachmentType: String = "", // MIME type, e.g. application/pdf
  val attachmentUrl: String = "",
  val maxPoints: Int = 20,
  val isCompletedByStudent: Boolean = false,
  val submissionStatus: String = "Pending", // Pending, Submitted, Graded
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027"
)

data class StudyMaterial(
  val id: String,
  val title: String,
  val subject: String,
  val classGrade: String,
  val chapter: String,
  val teacherName: String,
  val uploadDate: String,
  val fileType: String, // PDF, Document, Slide, Video
  val fileSize: String,
  val description: String,
  val downloadCount: Int = 42,
  val attachmentName: String = "",
  val attachmentType: String = "",
  val attachmentUrl: String = "",
  val schoolId: String = "revenex_school_001"
)

data class CheckedPaper(
  val id: String,
  val studentId: String,
  val studentName: String,
  val subject: String,
  val examName: String,
  val classGrade: String,
  val division: String,
  val marksObtained: Double,
  val maxMarks: Double,
  val teacherName: String,
  val uploadDate: String,
  val fileName: String,
  val fileType: String = "application/pdf",
  val fileUrl: String = "",
  val schoolId: String = "revenex_school_001"
)

data class AnswerKey(
  val id: String,
  val examName: String,
  val subject: String,
  val classGrade: String,
  val division: String,
  val teacherName: String,
  val fileName: String,
  val fileType: String = "application/pdf",
  val fileUrl: String = "",
  val uploadDate: String,
  val isPublished: Boolean = false,
  val schoolId: String = "revenex_school_001"
)

enum class NoticeCategory(val label: String) {
  CIRCULAR("Circular"),
  URGENT("Urgent Alert"),
  EXAM("Examination"),
  HOLIDAY("Holiday"),
  EVENT("School Event"),
  SPORTS("Sports & Culture")
}

data class SchoolNotice(
  val id: String,
  val title: String,
  val content: String,
  val category: NoticeCategory,
  val publishedDate: String,
  val authorName: String,
  val authorRole: String,
  val targetAudience: String, // "All School", "Students & Parents", "Faculty", "Class 10"
  val isImportant: Boolean = false,
  val attachmentName: String = "",
  val schoolId: String = "revenex_school_001"
)

enum class NotificationCategory(val label: String) {
  ATTENDANCE("Attendance"),
  FEES("Fees"),
  EXAMS("Examinations"),
  HOMEWORK("Homework"),
  ANNOUNCEMENTS("Notices"),
  LEAVE("Leave Requests"),
  EVENTS("Events"),
  SYSTEM("System")
}

data class ErpNotification(
  val id: String,
  val title: String,
  val message: String,
  val category: NotificationCategory,
  val timestamp: String,
  val isRead: Boolean = false,
  val targetRole: UserRole? = null,
  val actionableId: String = "",
  val schoolId: String = "revenex_school_001"
)

data class SchoolEvent(
  val id: String,
  val title: String,
  val description: String,
  val date: String,
  val time: String,
  val location: String,
  val category: String, // PTM, Cultural, Sports, Science, Holiday
  val organizer: String,
  val schoolId: String = "revenex_school_001"
)

data class TimetableSlot(
  val id: String,
  val dayOfWeek: String, // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  val periodNumber: Int,
  val startTime: String,
  val endTime: String,
  val subject: String,
  val teacherName: String,
  val roomNumber: String,
  val classGrade: String,
  val division: String,
  val schoolId: String = "revenex_school_001",
  val sessionId: String = "session_2026_2027"
) {
  companion object {
    fun hasConflict(existing: List<TimetableSlot>, newSlot: TimetableSlot): Boolean {
      return existing.any { slot ->
        slot.dayOfWeek.equals(newSlot.dayOfWeek, ignoreCase = true) &&
        slot.periodNumber == newSlot.periodNumber &&
        (
          (slot.classGrade.equals(newSlot.classGrade, ignoreCase = true) && slot.division.equals(newSlot.division, ignoreCase = true)) ||
          slot.teacherName.equals(newSlot.teacherName, ignoreCase = true)
        )
      }
    }
  }
}

data class TransportRoute(
  val id: String,
  val routeNumber: String,
  val routeName: String,
  val vehicleNumber: String,
  val driverName: String,
  val driverPhone: String,
  val totalCapacity: Int,
  val assignedStudents: Int,
  val stops: List<String>,
  val pickupStartTime: String,
  val dropStartTime: String,
  val monthlyFare: Long,
  val schoolId: String = "revenex_school_001"
) {
  companion object {
    fun hasAvailableCapacity(route: TransportRoute): Boolean {
      return route.assignedStudents < route.totalCapacity
    }
  }
}

data class LibraryBook(
  val id: String,
  val title: String,
  val author: String,
  val isbn: String,
  val category: String,
  val totalCopies: Int,
  val availableCopies: Int,
  val shelfLocation: String,
  val issuedCount: Int,
  val schoolId: String = "revenex_school_001"
) {
  companion object {
    fun isUniqueIsbn(existing: List<LibraryBook>, isbnToCheck: String): Boolean {
      return existing.none { it.isbn.trim().replace("-", "").equals(isbnToCheck.trim().replace("-", ""), ignoreCase = true) }
    }
  }
}

data class InventoryAsset(
  val id: String,
  val itemName: String,
  val category: String,
  val location: String,
  val quantity: Int,
  val condition: String, // Operational, Under Maintenance, Needs Replacement
  val purchaseDate: String,
  val estimatedValue: Long,
  val schoolId: String = "revenex_school_001"
) {
  companion object {
    fun getAssetsNeedingMaintenance(assets: List<InventoryAsset>): List<InventoryAsset> {
      return assets.filter { it.condition.equals("Needs Maintenance", ignoreCase = true) || it.condition.equals("Under Maintenance", ignoreCase = true) }
    }
  }
}
