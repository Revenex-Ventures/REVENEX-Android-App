package com.example.ui.screens.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.datasource.DataMode
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SchoolDocumentButton
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable internal fun TeacherSubmissionInbox(repository:ErpDataRepository,assignment:HomeworkAssignment) {
 val actor by repository.currentUser.collectAsState()
 val mode by repository.dataMode.collectAsState()
 val capabilities by repository.backendCapabilities.collectAsState()
 val ready=capabilities.loaded&&capabilities.submissions
 val all by repository.assignmentSubmissions.collectAsState()
 val students by repository.students.collectAsState()
 val submissions=all.filter{it.assignmentId==assignment.id && it.schoolId==actor.schoolId && it.sessionId==assignment.sessionId}.sortedByDescending{it.submittedAt}
 val roster=students.filter{it.schoolId==actor.schoolId && it.sessionId==assignment.sessionId && it.classGrade==assignment.classGrade && it.division==assignment.division}
 val missing=roster.filter{student->submissions.none{it.studentId==student.id}}
 var tab by rememberSaveable(actor.id,assignment.id){mutableIntStateOf(0)}
 var selected by rememberSaveable(actor.id,assignment.id){mutableStateOf("")}
 val reviewing=submissions.firstOrNull{it.id==selected}
 if(mode!=DataMode.SUPABASE_DEMO){EmptyStateView("School submission inbox","Sign in with your assigned school account to review shared submissions. Local Demo contains completion markers only.");return}
 if(reviewing!=null)SubmissionReviewDialog(repository,reviewing){selected=""}
 Column(Modifier.fillMaxSize().testTag("teacher_submission_inbox")){
  TeacherTabs(listOf(if(ready)"Received (${submissions.size})"else "Received",if(ready)"Missing (${missing.size})"else "Missing"),tab,{if(ready)tab=it})
  LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=TeacherLayout.margin,vertical=StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(TeacherLayout.gap)){
   if(!ready)item{EmptyStateView("School service needs setup","Submission availability could not be confirmed. Refresh after reconnect or ask your administrator to finish the backend update.")}
   else if(tab==0){
    if(submissions.isEmpty())item{EmptyStateView("No submissions yet","Confirmed student submissions appear here. Refresh after reconnect if updates were missed.")}
    items(submissions,key={it.id}){submission->TeacherCard{
     Column(Modifier.padding(TeacherLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){
      Text(submission.studentName.ifBlank{"Student record ${submission.studentId}"},style=MaterialTheme.typography.titleSmall)
      Text("${submission.reviewStatus.lowercase().replaceFirstChar{it.uppercase()}} · Version ${submission.version}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      if(submission.attachmentRef.isNotBlank())SchoolDocumentButton(submission.attachmentRef,submission.attachmentName.ifBlank{"Submission file"})
      if(submission.note.isNotBlank())Text(submission.note,maxLines=3,style=MaterialTheme.typography.bodySmall)
      OutlinedButton(onClick={selected=submission.id},modifier=Modifier.fillMaxWidth()){Text(if(submission.reviewStatus=="REVIEWED")"View / revise feedback"else "Review")}
     }
    }}
   }else{
    if(missing.isEmpty())item{EmptyStateView("No missing submissions","Every loaded student has a confirmed submission.")}
    items(missing,key={it.id}){student->ListItem(headlineContent={Text(student.name)},supportingContent={Text("Roll ${student.rollNumber} · Not submitted")})}
   }
   item{TextButton(onClick={repository.reconcileOnResume()}){Text("Refresh school status")}}
  }
 }
}

@Composable private fun SubmissionReviewDialog(repository:ErpDataRepository,submission:AssignmentSubmission,onClose:()->Unit) {
 var feedback by rememberSaveable(submission.id){mutableStateOf(submission.feedback)}
 var baseVersion by rememberSaveable(submission.id){mutableIntStateOf(submission.version)}
 var busy by remember{mutableStateOf(false)}
 var error by remember{mutableStateOf("")}
 val scope=rememberCoroutineScope()
 AlertDialog(onDismissRequest={if(!busy)onClose()},title={Text(submission.studentName.ifBlank{"Submission review"})},text={
  Column(Modifier.verticalScroll(rememberScrollState()).imePadding(),verticalArrangement=Arrangement.spacedBy(Spacing.md12)){
   Text("Version ${submission.version} · ${submission.reviewStatus.lowercase()}",style=MaterialTheme.typography.bodySmall)
   if(submission.attachmentRef.isNotBlank())SchoolDocumentButton(submission.attachmentRef,submission.attachmentName.ifBlank{"Open file"})
   if(submission.note.isNotBlank())Text(submission.note)
   OutlinedTextField(feedback,{feedback=it},Modifier.fillMaxWidth().testTag("submission_feedback"),label={Text("Feedback")},enabled=!busy,minLines=3,isError=feedback.length>4000,supportingText={Text("${feedback.length}/4000")})
   if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
   if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
   if(baseVersion!=submission.version)TextButton(enabled=!busy,onClick={feedback=submission.feedback;baseVersion=submission.version}){Text("Load saved feedback · Version ${submission.version}")}
  }
 },confirmButton={TextButton(enabled=!busy&&feedback.trim().length in 1..4000,onClick={
  busy=true;error="";scope.launch{try{repository.reviewSubmissionConfirmed(submission.id,feedback,baseVersion);onClose()}
   catch(e:CancellationException){throw e}catch(_:Exception){error="Review was not confirmed. Your feedback is retained. Refresh submissions to check access or a newer version before retrying."}finally{busy=false}}
 }){Text(if(busy)"Saving…"else "Save review")}},dismissButton={TextButton(enabled=!busy,onClick=onClose){Text("Close")}})
}
