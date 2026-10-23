package com.example.ui.screens.academics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.Spacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Existing Results route, staff-only presentation; RPC/RLS rechecks authorization. */
@Composable
fun StaffMarksWorkspace(repository: ErpDataRepository, onViewReports: () -> Unit = {}) {
  val actor by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val reports by repository.reportCards.collectAsState()
  var selectedId by rememberSaveable(actor.id) { mutableStateOf("") }
  var query by rememberSaveable(actor.id) { mutableStateOf("") }
  val student = students.firstOrNull { it.id == selectedId } ?: students.firstOrNull()
  val scope = rememberCoroutineScope()
  var busy by remember(actor.id) { mutableStateOf(false) }
  var feedback by remember(actor.id) { mutableStateOf<String?>(null) }
  var failed by remember(actor.id) { mutableStateOf(false) }
  var confirmPublish by remember(actor.id, student?.id) { mutableStateOf(false) }
  var term by rememberSaveable(actor.id, student?.id) { mutableStateOf("") }
  var subject by rememberSaveable(actor.id, student?.id) { mutableStateOf("") }
  var obtained by rememberSaveable(actor.id, student?.id) { mutableStateOf("") }
  var maximum by rememberSaveable(actor.id, student?.id) { mutableStateOf("100") }
  val report = reports.firstOrNull { it.studentId == student?.id && it.term == term.trim() }
  val score = obtained.toIntOrNull()
  val total = maximum.toIntOrNull()
  fun perform(message: String, operation: suspend () -> Unit) {
    busy = true; feedback = null; failed = false
    scope.launch {
      try { operation(); feedback = message }
      catch (e: Exception) {
        if (e is CancellationException) throw e
        failed = true
        feedback = "Not confirmed. Check your access, connection and school setup; reload before retrying. Published terms must be unpublished before editing."
      } finally { busy = false }
    }
  }
  if (confirmPublish && student != null && report != null) AlertDialog(
    onDismissRequest = { if (!busy) confirmPublish = false },
    title = { Text("Publish results?") },
    text = { Text("${student.name} · ${report.term}\n${report.scores.size} subject(s). Linked students and guardians will be able to read these marks and receive an in-app update.") },
    confirmButton = { TextButton(enabled = !busy, onClick = {
      confirmPublish = false
      perform("Publication confirmed.") { repository.publishResultsConfirmed(student.id, report.term, true) }
    }) { Text("Publish") } },
    dismissButton = { TextButton(onClick = { confirmPublish = false }) { Text("Cancel") } })

  LazyColumn(Modifier.fillMaxSize().imePadding().testTag("staff_marks_workspace"),
    contentPadding = PaddingValues(Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.md12)) {
    item {
      Text("Marks & publication", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text("Save private drafts, review, then publish to linked families.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    item { TextButton(onClick = onViewReports, enabled = !busy) { Text("View report cards & student details") } }
    if (students.isEmpty()) item { EmptyStateView("No accessible students", "Your school must allocate a class before marks can be entered.") }
    else {
      item {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Find student or class") }, enabled = !busy, singleLine = true)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          items(students.filter { it.name.contains(query, true) || it.fullClass.contains(query, true) }, key = { it.id }) { candidate ->
            FilterChip(student?.id == candidate.id, { selectedId = candidate.id; feedback = null },
              { Text("${candidate.name} · ${candidate.fullClass}") }, enabled = !busy)
          }
        }
      }
      item {
        Text(student!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(term, { term = it; feedback = null }, Modifier.fillMaxWidth().testTag("marks_term"),
          label = { Text("Term / assessment") }, enabled = !busy, singleLine = true)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          items(reports.filter { it.studentId == student.id }.map { it.term }.distinct()) { savedTerm ->
            FilterChip(term == savedTerm, { term = savedTerm; feedback = null }, { Text(savedTerm) }, enabled = !busy)
          }
        }
      }
      item {
        Text(if (report?.published == true) "Published · Unpublish before editing" else "Private draft", style = MaterialTheme.typography.titleSmall)
        report?.scores?.forEach { mark ->
          ListItem(headlineContent = { Text(mark.subjectName) }, trailingContent = { Text("${mark.obtainedMarks}/${mark.maxMarks}") })
        }
        if (report == null) Text("No saved marks in this term.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      item {
        val editable = !busy && report?.published != true
        OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth().testTag("marks_subject"), label = { Text("Subject") }, enabled = editable, singleLine = true)
        Spacer(Modifier.height(Spacing.sm8))
        OutlinedTextField(obtained, { obtained = it }, Modifier.fillMaxWidth().testTag("marks_obtained"), label = { Text("Marks obtained") }, enabled = editable,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Spacer(Modifier.height(Spacing.sm8))
        OutlinedTextField(maximum, { maximum = it }, Modifier.fillMaxWidth().testTag("marks_maximum"), label = { Text("Maximum marks") }, enabled = editable,
          supportingText = { Text("0 ≤ obtained ≤ maximum · Maximum 10,000") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Button(modifier = Modifier.fillMaxWidth().testTag("save_draft_mark"), enabled = editable && term.isNotBlank() && subject.isNotBlank() && score != null && total != null && total in 1..10000 && score in 0..total,
          onClick = { perform("Draft save confirmed.") { repository.saveMarksConfirmed(student!!.id, term, subject, score!!, total!!) } }) { Text(if (busy) "Saving…" else "Save private draft") }
      }
      if (report != null) item {
        OutlinedButton(enabled = !busy && report.scores.isNotEmpty(), modifier = Modifier.fillMaxWidth(), onClick = {
          if (report.published) perform("Unpublication confirmed.") { repository.publishResultsConfirmed(student!!.id, report.term, false) }
          else confirmPublish = true
        }) { Text(if (report.published) "Unpublish term" else "Review & publish term") }
      }
    }
    feedback?.let { message -> item { Text(message, color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) } }
  }
}
