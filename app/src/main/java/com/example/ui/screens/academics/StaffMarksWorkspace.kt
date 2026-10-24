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
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.Radius
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SchoolFilterChip
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
  var choosing by remember {mutableStateOf(false)}
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

  LazyColumn(Modifier.fillMaxSize().imePadding().testTag("staff_marks_workspace"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    item {WorkspacePageHeader("Marks & publication")}
    item {TextButton(onClick=onViewReports,enabled=!busy){Text("Report cards")}}
    if(students.isEmpty())item {EmptyStateView("No accessible students","Your school must allocate a class before marks can be entered.")}
    else {
      item {SchoolSection("1 · Student") {
        OutlinedTextField(query,{query=it;choosing=true},Modifier.fillMaxWidth(),label={Text("Find student or class")},enabled=!busy,singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.input))
        Box {
          OutlinedButton(onClick={choosing=true},enabled=!busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.input)){Text("${student!!.name} · ${student.fullClass}");Icon(Icons.Default.ExpandMore,null)}
          DropdownMenu(expanded=choosing,onDismissRequest={choosing=false}) {
            val matches=students.filter{it.name.contains(query,true)||it.fullClass.contains(query,true)}
            if(matches.isEmpty())DropdownMenuItem(text={Text("No matching student")},enabled=false,onClick={})
            matches.forEach {candidate->DropdownMenuItem(text={Text("${candidate.name} · ${candidate.fullClass}")},onClick={selectedId=candidate.id;feedback=null;choosing=false},enabled=!busy)}
          }
        }
      }}
      item {SchoolSection("2 · Assessment") {
        OutlinedTextField(term,{term=it;feedback=null},Modifier.fillMaxWidth().testTag("marks_term"),label={Text("Term / assessment")},isError=term.length>80,enabled=!busy,singleLine=true,shape=RoundedCornerShape(Radius.input))
        LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {items(reports.filter{it.studentId==student!!.id}.map{it.term}.distinct()){savedTerm->SchoolFilterChip(term==savedTerm,{term=savedTerm;feedback=null},{Text(savedTerm)},enabled=!busy)}}
      }}
      item {SchoolSection("Saved subjects") {SchoolGroup {
        Row(Modifier.fillMaxWidth().padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
          Text(if(report?.published==true)"Published · Unpublish before editing" else "Private draft",style=MaterialTheme.typography.titleSmall)
          Icon(if(report?.published==true)Icons.Default.Visibility else Icons.Default.Lock,null,tint=MaterialTheme.colorScheme.primary)
        }
        if(report==null)Text("No saved marks in this term.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        report?.scores?.forEach{mark->HorizontalDivider();Row(Modifier.fillMaxWidth().padding(Spacing.md12),horizontalArrangement=Arrangement.SpaceBetween){Text(mark.subjectName,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Text("${mark.obtainedMarks}/${mark.maxMarks}",style=MaterialTheme.typography.titleSmall)}}
      }}}
      item {SchoolSection("3 · Enter marks") {
        val editable=!busy&&report?.published!=true
        OutlinedTextField(subject,{subject=it},Modifier.fillMaxWidth().testTag("marks_subject"),label={Text("Subject")},isError=subject.length>120,enabled=editable,singleLine=true,shape=RoundedCornerShape(Radius.input))
        ResponsiveFormFields(
          first = { field -> OutlinedTextField(obtained,{obtained=it},field.testTag("marks_obtained"),label={Text("Marks obtained")},isError=obtained.isNotBlank()&&(score==null||score<0||(total!=null&&score>total)),enabled=editable,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,shape=RoundedCornerShape(Radius.input)) },
          second = { field -> OutlinedTextField(maximum,{maximum=it},field.testTag("marks_maximum"),label={Text("Maximum marks")},isError=maximum.isNotBlank()&&(total==null||total !in 1..10000),enabled=editable,supportingText={Text("0 ≤ obtained ≤ maximum · Maximum 10,000")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,shape=RoundedCornerShape(Radius.input)) }
        )
        Button(modifier=Modifier.fillMaxWidth().testTag("save_draft_mark"),shape=RoundedCornerShape(Radius.input),enabled=editable&&term.trim().length in 1..80&&subject.trim().length in 1..120&&score!=null&&total!=null&&total in 1..10000&&score in 0..total,
          onClick={perform("Draft save confirmed."){repository.saveMarksConfirmed(student!!.id,term,subject,score!!,total!!)}}){Text(if(busy)"Saving…" else "Save draft")}
      }}
      if(report!=null)item {
        OutlinedButton(enabled=!busy&&report.scores.isNotEmpty(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.input),onClick={
          if(report.published)perform("Unpublication confirmed."){repository.publishResultsConfirmed(student!!.id,report.term,false)} else confirmPublish=true
        }){Text(if(report.published)"Unpublish term" else "Review & publish")}
      }
    }
    feedback?.let{message->item{Surface(color=if(failed)MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(Radius.md)) {
      Text(message,Modifier.padding(Spacing.md12),color=if(failed)MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,style=MaterialTheme.typography.bodyMedium)
    }}}
  }
}
