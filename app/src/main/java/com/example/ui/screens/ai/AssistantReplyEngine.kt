package com.example.ui.screens.ai

import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import java.util.Locale

/**
 * A deliberately bounded assistant for builds without a configured live model.
 *
 * It never infers school facts from the prompt.  Personal summaries are derived
 * only from the records already scoped to the signed-in person in the app, and
 * consequential requests remain in the regular, confirmed ERP workflows.
 */
data class AssistantContext(
  val user: UserProfile,
  val students: List<Student>,
  val feeRecords: List<FeeRecord>,
  val isDemo: Boolean
)

enum class AssistantReplySource { GENERAL_GUIDANCE, LOADED_RECORDS, ERP_GUIDANCE, UNAVAILABLE, SERVER_AI, SERVER_ERP, SERVER_DEMO }

data class AssistantReply(
  val text: String,
  val source: AssistantReplySource
)

object AssistantReplyEngine {
  fun reply(query: String, context: AssistantContext): AssistantReply {
    val normalized = query.trim().lowercase(Locale.ROOT)
    if (normalized.isBlank()) return unavailable()

    if (isConsequentialRequest(normalized)) return AssistantReply(
      "I cannot publish, change, create, delete, send, or record school data from this chat. " +
        "Use Notices for announcements, or the relevant ERP screen, where the change can be reviewed, authorised, and saved with an audit record.",
      AssistantReplySource.ERP_GUIDANCE
    )

    if (normalized.containsAny("hello", "hi", "namaste", "good morning", "good evening")) {
      return AssistantReply(
        "Hello ${context.user.name}. I can explain study topics, help you find the right ERP workflow, " +
          "or summarize your permitted loaded records. ${sourceNote(context)}",
        AssistantReplySource.GENERAL_GUIDANCE
      )
    }

    if (normalized.containsAny("attendance", "present", "absent")) return attendanceReply(context)
    if (normalized.containsAny("fee", "fees", "payment", "due", "invoice")) return feeReply(context)
    if (normalized.containsAny("timetable", "schedule", "period")) return navigationReply(
      "Open Timetable to view the current published class schedule. I cannot verify a timetable detail from this chat.",
      context
    )
    if (normalized.containsAny("notice", "notices", "circular", "announcement")) return navigationReply(
      "Open Notices to read the current published circulars. I cannot confirm an announcement that is not available in the loaded records.",
      context
    )
    if (normalized.containsAny("math", "algebra", "fraction", "equation")) return AssistantReply(
      "General study guidance: solve an equation by applying the same operation to both sides. " +
        "For example, in 3x + 4 = 19, subtract 4, then divide by 3, so x = 5. " +
        "Share the exact question and I can explain the steps.",
      AssistantReplySource.GENERAL_GUIDANCE
    )
    if (normalized.containsAny("science", "photosynthesis", "force", "biology", "physics")) return AssistantReply(
      "General study guidance: photosynthesis is the process in which plants use light, water, and carbon dioxide to make glucose and release oxygen. " +
        "Tell me the chapter or question and I will explain it at the right level.",
      AssistantReplySource.GENERAL_GUIDANCE
    )
    if (normalized.containsAny("english", "grammar", "essay", "tense")) return AssistantReply(
      "General study guidance: start by identifying the subject, verb, and tense. " +
        "For an essay, make one clear point per paragraph and support it with an example. " +
        "Paste a sentence or topic if you want help improving it.",
      AssistantReplySource.GENERAL_GUIDANCE
    )

    return unavailable()
  }

  private fun attendanceReply(context: AssistantContext): AssistantReply {
    val people = linkedStudents(context)
    if (people.isEmpty()) return navigationReply(
      "I cannot verify attendance for this account from the loaded records. Open Attendance to view the authorised register.",
      context
    )
    val summary = people.joinToString(separator = "\n") { student ->
      "• ${student.name}: ${"%.1f".format(Locale.ROOT, student.attendancePercent)}% recorded attendance"
    }
    return AssistantReply(
      "Here is the attendance summary for your linked ${if (people.size == 1) "record" else "children"}:\n$summary\n${sourceNote(context)}",
      AssistantReplySource.LOADED_RECORDS
    )
  }

  private fun feeReply(context: AssistantContext): AssistantReply {
    val people = linkedStudents(context)
    if (people.isEmpty()) return navigationReply(
      "I cannot access a fee record for this account from the loaded records. Open Fees to view the authorised ledger.",
      context
    )
    val summary = people.joinToString(separator = "\n") { student ->
      val fee = context.feeRecords.firstOrNull { it.studentId == student.id }
      if (fee == null) "• ${student.name}: no loaded fee record is available."
      else "• ${student.name}: ₹${fee.pendingAmount / 100} pending (${fee.status.label})."
    }
    return AssistantReply(
      "Fee summary for your linked ${if (people.size == 1) "record" else "children"}:\n$summary\n${sourceNote(context)}",
      AssistantReplySource.LOADED_RECORDS
    )
  }

  private fun navigationReply(message: String, context: AssistantContext) = AssistantReply(
    "$message ${sourceNote(context)}",
    AssistantReplySource.ERP_GUIDANCE
  )

  private fun unavailable() = AssistantReply(
    "That information is not available from the verified records loaded in this app. " +
      "Live AI is not configured, so I will not invent an answer. You can ask for general study help or use the relevant ERP module.",
    AssistantReplySource.UNAVAILABLE
  )

  private fun sourceNote(context: AssistantContext) = if (context.isDemo) {
    "Demo data is shown; it is not live school information."
  } else {
    "This summary uses only records currently loaded for your account."
  }

  private fun linkedStudents(context: AssistantContext): List<Student> = when (context.user.role) {
    UserRole.STUDENT -> context.students.filter { it.id == context.user.associatedStudentId }
    UserRole.PARENT -> {
      val childNames = context.user.associatedChildNames.map { it.substringBefore(" (").trim() }.toSet()
      context.students.filter { it.id == context.user.associatedStudentId || it.name in childNames }
    }
    else -> emptyList()
  }

  private fun isConsequentialRequest(query: String): Boolean = query.containsAny(
    "add student", "add teacher", "create", "publish", "record", "mark attendance",
    "update", "change", "delete", "send notice", "send message", "pay fee"
  )

  private fun String.containsAny(vararg fragments: String): Boolean = fragments.any(::contains)
}
