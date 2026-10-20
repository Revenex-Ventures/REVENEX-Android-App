package com.example

import com.example.data.model.UserRole
import com.example.data.repository.SampleData
import com.example.ui.screens.ai.AssistantContext
import com.example.ui.screens.ai.AssistantReplyEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantReplyEngineTest {
  private val parent = SampleData.defaultProfiles.first { it.role == UserRole.PARENT }
  private val student = SampleData.defaultProfiles.first { it.role == UserRole.STUDENT }

  @Test
  fun parent_fee_answer_is_limited_to_linked_children() {
    val reply = AssistantReplyEngine.reply(
      query = "Show Ananya Sharma's fee status",
      context = AssistantContext(parent, SampleData.initialStudents, SampleData.initialFeeRecords, isDemo = true)
    )

    assertFalse(reply.text.contains("Ananya Sharma"))
    assertTrue(reply.text.contains("linked child"))
    assertTrue(reply.text.contains("Demo data"))
  }

  @Test
  fun student_attendance_answer_does_not_disclose_classmates() {
    val reply = AssistantReplyEngine.reply(
      query = "What is my attendance?",
      context = AssistantContext(student, SampleData.initialStudents, SampleData.initialFeeRecords, isDemo = true)
    )

    assertTrue(reply.text.contains("Aarav Patel"))
    assertFalse(reply.text.contains("Ananya Sharma"))
  }

  @Test
  fun consequential_request_is_redirected_to_confirmed_erp_workflow() {
    val reply = AssistantReplyEngine.reply(
      query = "Publish a notice to all parents",
      context = AssistantContext(parent, SampleData.initialStudents, SampleData.initialFeeRecords, isDemo = true)
    )

    assertTrue(reply.text.contains("cannot publish"))
    assertTrue(reply.text.contains("Notices"))
  }

  @Test
  fun unavailable_school_question_never_invents_an_answer() {
    val reply = AssistantReplyEngine.reply(
      query = "What is tomorrow's bus timing?",
      context = AssistantContext(student, SampleData.initialStudents, SampleData.initialFeeRecords, isDemo = false)
    )

    assertTrue(reply.text.contains("not available"))
    assertTrue(reply.text.contains("Live AI is not configured"))
  }
}
