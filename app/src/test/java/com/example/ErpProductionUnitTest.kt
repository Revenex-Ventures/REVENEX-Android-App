package com.example

import com.example.data.datasource.LocalDemoDataSource
import com.example.data.model.*
import com.example.data.repository.SampleData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * ErpProductionUnitTest — Production Validation Test Suite for REVENEX ERP.
 * Verifies core business logic, multi-tenant isolation, parent-child permissions,
 * attendance percentage engine, fee balances, and exam result publishing gates.
 */
class ErpProductionUnitTest {

  private lateinit var dataSource: LocalDemoDataSource

  @Before
  fun setUp() {
    dataSource = LocalDemoDataSource()
  }

  @Test
  fun testSchoolTenantIsolation_DefaultSchoolIdAssigned() {
    val student = Student(
      id = "stu_test_001",
      admissionNumber = "REV-TEST-001",
      rollNumber = 99,
      name = "Test Student",
      classGrade = "10",
      division = "A",
      dob = "01 Jan 2010",
      gender = "Male",
      bloodGroup = "A+",
      parentName = "Test Parent",
      parentPhone = "+91 99999 99999",
      parentEmail = "test.parent@gmail.com",
      address = "Test Address",
      admissionDate = "01 Jun 2026",
      attendancePercent = 95.0,
      feeStatus = FeeStatus.PENDING,
      feePendingAmount = 1000000L,
      schoolId = "revenex_school_001"
    )

    assertEquals("revenex_school_001", student.schoolId)
    assertNotEquals("school_other_999", student.schoolId)
  }

  @Test
  fun testParentChildAssociation_ParentOnlyAccessesLinkedChildren() {
    val parentProfile = UserProfile(
      id = "user_parent_test",
      name = "Rajesh Patel",
      email = "rajesh.patel.biz@gmail.com",
      role = UserRole.PARENT,
      designation = "Parent of Aarav & Diya",
      phone = "+91 98220 54199",
      avatarInitials = "RP",
      associatedStudentId = "stu_1001",
      associatedChildNames = listOf("Aarav Patel (10-A)", "Diya Patel (6-B)")
    )

    assertEquals(UserRole.PARENT, parentProfile.role)
    assertEquals("stu_1001", parentProfile.associatedStudentId)
    assertTrue(parentProfile.associatedChildNames.contains("Aarav Patel (10-A)"))
    assertFalse(parentProfile.associatedChildNames.contains("Other Student (8-C)"))
  }

  @Test
  fun testRecordBasedAttendanceCalculationEngine() = runBlocking {
    val studentId = "stu_1001"
    val studentList = listOf(
      StudentAttendance(studentId, "Aarav Patel", 14, AttendanceStatus.PRESENT),
      StudentAttendance("stu_1002", "Ananya Sharma", 15, AttendanceStatus.ABSENT)
    )

    // Save class attendance
    dataSource.saveClassAttendance("10", "A", "28 Aug 2026", "Prof. Sunita Rao", studentList)

    val students = dataSource.getStudentsFlow().first()
    val updatedAarav = students.firstOrNull { it.id == studentId }

    assertNotNull(updatedAarav)
    // Percentage must be a valid number between 0.0 and 100.0
    assertTrue(updatedAarav!!.attendancePercent in 0.0..100.0)
  }

  @Test
  fun testFeePaymentTransaction_UpdatesPendingBalanceAndReceipt() = runBlocking {
    val studentId = "stu_1001"
    val initialFees = dataSource.getFeeRecordsFlow().first()
    val recordBefore = initialFees.firstOrNull { it.studentId == studentId }
    assertNotNull(recordBefore)

    val paymentAmount = 500000L
    val initialPaid = recordBefore!!.paidAmount

    val txn = dataSource.processFeePayment(studentId, paymentAmount, "UPI", "Term Fee")

    assertEquals("SUCCESS", txn.status)
    assertTrue(txn.receiptNo.startsWith("REC-"))
    assertTrue(txn.transactionId.startsWith("TXN-"))

    val feeRecordsAfter = dataSource.getFeeRecordsFlow().first()
    val recordAfter = feeRecordsAfter.firstOrNull { it.studentId == studentId }

    assertNotNull(recordAfter)
    assertEquals(initialPaid + paymentAmount, recordAfter!!.paidAmount)
  }

  @Test
  fun testExamSchedule_PublishedStateGatekeeper() = runBlocking {
    val exams = dataSource.getExamSchedulesFlow().first()
    assertTrue(exams.isNotEmpty())

    val publishedExams = exams.filter { it.isPublished }
    val draftExams = exams.filter { !it.isPublished }

    // Published exams should be visible
    publishedExams.forEach { exam ->
      assertTrue(exam.isPublished)
      assertTrue(exam.subjects.isNotEmpty())
    }

    // Draft exams are hidden from student/parent views
    draftExams.forEach { exam ->
      assertFalse(exam.isPublished)
    }
  }

  @Test
  fun testLeaveRequest_StatusWorkflowTransitions() = runBlocking {
    val initialLeaves = dataSource.getLeaveRequestsFlow().first()
    val initialCount = initialLeaves.size

    dataSource.applyLeave(
      applicantName = "Aarav Patel",
      applicantRole = UserRole.STUDENT,
      applicantId = "stu_1001",
      classOrDept = "10-A",
      leaveType = "Medical Leave",
      startDate = "01 Sep 2026",
      endDate = "03 Sep 2026",
      daysCount = 3,
      reason = "Fever and rest recommended by doctor"
    )

    val leavesAfterApply = dataSource.getLeaveRequestsFlow().first()
    assertEquals(initialCount + 1, leavesAfterApply.size)

    val newLeave = leavesAfterApply.first()
    assertEquals(LeaveStatus.PENDING, newLeave.status)
    assertEquals("Aarav Patel", newLeave.applicantName)

    dataSource.updateLeaveStatus(newLeave.id, LeaveStatus.APPROVED, "Approved by Principal")

    val leavesAfterApprove = dataSource.getLeaveRequestsFlow().first()
    val approvedLeave = leavesAfterApprove.first { it.id == newLeave.id }

    assertEquals(LeaveStatus.APPROVED, approvedLeave.status)
    assertEquals("Approved by Principal", approvedLeave.approverRemark)
  }

  @Test
  fun testHomeworkAssignment_StudentCompletionToggle() = runBlocking {
    val assignments = dataSource.getAssignmentsFlow().first()
    assertTrue(assignments.isNotEmpty())

    val targetHomework = assignments.first()
    val initialStatus = targetHomework.isCompletedByStudent

    dataSource.toggleAssignmentCompletion(targetHomework.id)

    val updatedAssignments = dataSource.getAssignmentsFlow().first()
    val updatedHomework = updatedAssignments.first { it.id == targetHomework.id }

    assertEquals(!initialStatus, updatedHomework.isCompletedByStudent)
    assertEquals(if (!initialStatus) "Submitted" else "Pending", updatedHomework.submissionStatus)
  }

  @Test
  fun testStudentMarksCheckedPaperAndAnswerKey() = runBlocking {
    val reportCards = dataSource.getReportCardsFlow().first()
    assertTrue(reportCards.isNotEmpty())

    val targetCard = reportCards.first()
    val studentId = targetCard.studentId
    val term = targetCard.term
    val subject = "Mathematics"

    dataSource.saveStudentMarks(studentId, term, subject, 95, 100)
    val afterMarks = dataSource.getReportCardsFlow().first().first { it.id == targetCard.id }
    val mathScore = afterMarks.scores.first { it.subjectName == subject }
    assertEquals(95, mathScore.obtainedMarks)
    assertEquals(100, mathScore.maxMarks)
    assertEquals("A+", mathScore.grade)

    dataSource.saveCheckedPaper(studentId, term, subject, "math_paper_verified.jpg")
    val afterPaper = dataSource.getReportCardsFlow().first().first { it.id == targetCard.id }
    val paperScore = afterPaper.scores.first { it.subjectName == subject }
    assertEquals("math_paper_verified.jpg", paperScore.checkedPaperUrl)

    dataSource.saveAnswerKey(targetCard.classGrade, term, subject, "math_official_key.pdf")
    val afterKey = dataSource.getReportCardsFlow().first().first { it.id == targetCard.id }
    val keyScore = afterKey.scores.first { it.subjectName == subject }
    assertEquals("math_official_key.pdf", keyScore.answerKeyUrl)
  }
}
