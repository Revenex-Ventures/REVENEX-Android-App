package com.example

import com.example.data.datasource.*
import com.example.data.repository.SampleData
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class SupabaseStudentSummaryTest {
  @Test fun absentFinancialAccessNeverUsesDuplicatedStudentMoney() {
    val summary=summarizeSupabaseStudent(SampleData.initialStudents.first(),emptyList(),emptyList())
    assertFalse(summary.feeSummaryAvailable)
    assertEquals(0L,summary.feePendingAmount)
    assertEquals(0.0,summary.attendancePercent,0.001)
    assertEquals(0,summary.rank)
  }
  @Test fun attendanceUsesStructuredRecordsInsteadOfSeedPercentage() {
    val student=SampleData.initialStudents.first()
    val records=listOf("PRESENT","LATE","ABSENT").mapIndexed { i,status ->
      SupabaseRow("a$i",student_id=student.id,status=status)
    }+SupabaseRow("foreign",student_id="other",status="ABSENT")
    val summary=summarizeSupabaseStudent(student,emptyList(),records)
    assertEquals(200.0/3.0,summary.attendancePercent,0.001)
  }
  @Test fun financialSummaryIncludesOnlyMatchingAuthorizedFeeRows() {
    val student=SampleData.initialStudents.first()
    val fee=SampleData.initialFeeRecords.first().copy(studentId=student.id,totalFee=10000L,paidAmount=3000L)
    val foreign=fee.copy(id="other",studentId="other",totalFee=90000L)
    val summary=summarizeSupabaseStudent(student,listOf(fee,foreign),emptyList())
    assertTrue(summary.feeSummaryAvailable)
    assertEquals(7000L,summary.feePendingAmount)
  }
  @Test fun excusedCountsAsAttendedAndLeaveRemainsInRecordedSessionDenominator() {
    val student=SampleData.initialStudents.first()
    val records=listOf("PRESENT","LATE","EXCUSED","ABSENT","LEAVE").mapIndexed { i,status ->
      SupabaseRow("status$i",student_id=student.id,status=status)
    }+SupabaseRow("unknown",student_id=student.id,status="UNKNOWN")
    assertEquals(60.0,summarizeSupabaseStudent(student,emptyList(),records).attendancePercent,0.001)
  }
}

