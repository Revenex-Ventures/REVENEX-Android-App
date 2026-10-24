package com.example

import com.example.data.repository.SampleData
import com.example.ui.screens.academics.reportsForMode
import org.junit.Assert.*
import org.junit.Test

class ReportDataIntegrityTest {
  @Test fun cloudNeverSynthesizesResultsWhenThereAreNoAuthorizedMarks() {
    val student = SampleData.initialStudents.first()
    assertTrue(reportsForMode(student, emptyList(), cloud=true).isEmpty())
    assertTrue(reportsForMode(student, emptyList(), cloud=false).isNotEmpty())
  }
  @Test fun cloudReportsContainOnlyTheSelectedStudentsRealRows() {
    val student = SampleData.initialStudents.first()
    val rows = SampleData.initialReportCards
    assertEquals(rows.filter { it.studentId == student.id }, reportsForMode(student, rows, cloud=true))
  }
}
