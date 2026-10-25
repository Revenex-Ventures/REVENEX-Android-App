package com.example
import com.example.data.datasource.*
import com.example.data.model.HomeworkAssignment
import org.junit.Assert.*
import org.junit.Test
class ParentAssignmentContextTest {
 private val work=HomeworkAssignment("work","Task","Math","10","A","Teacher","2026-10-24","2026-10-25","Instructions")
 @Test fun switchingLinkedChildDoesNotCarryTheFirstChildsCompletion() {
  val submissions=listOf(SupabaseRow("submission",student_id="childA",assignment_id="work",completed=true))
  assertTrue(assignmentForStudent(work,submissions,"childA").isCompletedByStudent)
  assertFalse(assignmentForStudent(work,submissions,"childB").isCompletedByStudent)
  assertEquals("Pending",assignmentForStudent(work,submissions,"childB").submissionStatus)
 }
 @Test fun blankIdentityAndAnotherAssignmentCannotProduceSubmittedState() {
  val submissions=listOf(SupabaseRow("submission",student_id="childA",assignment_id="different",completed=true))
  assertFalse(assignmentForStudent(work,submissions,"childA").isCompletedByStudent)
  assertFalse(assignmentForStudent(work,submissions,"").isCompletedByStudent)
 }
}
