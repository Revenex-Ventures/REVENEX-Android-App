package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var repository: ErpDataRepository

  @Before
  fun setUp() {
    repository = ErpDataRepository.getInstance()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Revenex ERP", appName)
  }

  @Test
  fun `test attendance marking and dynamic percentage update`() {
    val initialStudents = repository.students.value
    assertTrue(initialStudents.isNotEmpty())

    val targetStudent = initialStudents.first()
    val testList = listOf(
      StudentAttendance(
        studentId = targetStudent.id,
        studentName = targetStudent.name,
        rollNumber = targetStudent.rollNumber,
        status = AttendanceStatus.PRESENT
      )
    )

    repository.saveClassAttendance(
      classGrade = "10",
      division = "A",
      date = "28 Aug 2026",
      markedBy = "Prof. Sunita Rao",
      studentList = testList
    )
    ShadowLooper.idleMainLooper()

    val todayStatus = repository.getStudentTodayAttendance(targetStudent.id)
    assertEquals(AttendanceStatus.PRESENT, todayStatus)
  }

  @Test
  fun `test leave application and principal approval flow`() {
    val initialLeavesCount = repository.leaveRequests.value.size

    repository.applyLeave(
      applicantName = "Prof. Sunita Rao",
      applicantRole = UserRole.TEACHER,
      applicantId = "tch_201",
      classOrDept = "Mathematics",
      leaveType = "Medical Leave",
      startDate = "01 Sep 2026",
      endDate = "03 Sep 2026",
      daysCount = 3,
      reason = "Attending academic conference"
    )
    ShadowLooper.idleMainLooper()

    val updatedLeaves = repository.leaveRequests.value
    assertTrue(updatedLeaves.size >= initialLeavesCount + 1)
    val latestLeave = updatedLeaves.first()
    assertEquals(LeaveStatus.PENDING, latestLeave.status)

    repository.approveLeave(latestLeave.id, "Approved by Principal")
    ShadowLooper.idleMainLooper()
    
    val approvedLeave = repository.leaveRequests.value.first { it.id == latestLeave.id }
    assertEquals(LeaveStatus.APPROVED, approvedLeave.status)
  }

  @Test
  fun `test fee payment processing and balance reduction`() {
    val student = repository.students.value.first()
    val paymentAmount = 5000.0

    repository.processFeePayment(
      studentId = student.id,
      amountPaid = paymentAmount,
      paymentMethod = "UPI / PhonePe",
      feeHead = "Tuition Installment"
    )
    ShadowLooper.idleMainLooper()

    val feeRecord = repository.feeRecords.value.first { it.studentId == student.id }
    assertTrue(feeRecord.transactions.isNotEmpty())
  }

  @Test
  fun `test homework creation and completion toggle`() {
    val assignment = HomeworkAssignment(
      id = "hw_test_${System.currentTimeMillis()}",
      title = "Quadratic Equations Exercise",
      subject = "Mathematics",
      classGrade = "10",
      division = "A",
      teacherName = "Prof. Sunita Rao",
      assignedDate = "28 Aug 2026",
      dueDate = "02 Sep 2026",
      instructions = "Solve questions 1 through 15 on Chapter 4.",
      maxPoints = 20
    )

    repository.createAssignment(assignment)
    ShadowLooper.idleMainLooper()
    
    val found = repository.assignments.value.firstOrNull { it.id == assignment.id }
    assertNotNull(found)

    repository.toggleAssignmentCompletion(assignment.id)
    ShadowLooper.idleMainLooper()
    
    val toggled = repository.assignments.value.firstOrNull { it.id == assignment.id }
    assertTrue(toggled?.isCompletedByStudent == true)
  }
}

