package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable fun StudentAssignments(repository:ErpDataRepository,navigate:(String)->Unit) {
 val student=ownStudent(repository);val assignments by repository.assignments.collectAsState()
 var tab by rememberSaveable{mutableIntStateOf(0)}
 val own=assignments.filter{student!=null&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division}
 val visible=own.filter{when(tab){1->!it.isCompletedByStudent;2->it.isCompletedByStudent;else->true}}.sortedBy{studentDate(it.dueDate)}
 LazyColumn(Modifier.fillMaxSize().testTag("student_assignments"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)) {
  item{WorkspacePageHeader("Assignments")}
  item{StudentTabs(listOf("All","Due","Done"),tab){tab=it}}
  if(visible.isEmpty())item{EmptyStateView("No assignments","Work available to your class will appear here.")}
  items(visible,key={it.id}){work->StudentRow(work.title,"${work.subject} · ${work.classGrade}-${work.division}\nDue ${work.dueDate}",StudentIcons.Document,if(work.isCompletedByStudent)StudentTone.MINT else if(studentSubjectTone(work.subject)==StudentTone.LAVENDER)StudentTone.LAVENDER else StudentTone.BLUE,{navigate(Screen.StudentAssignment.createRoute(work.id))},if(work.isCompletedByStudent)"Submitted"else "Due",statusTone=if(work.isCompletedByStudent)StudentTone.MINT else StudentTone.ORANGE)}
 }
}

/** Uses the existing confirmed completion API. It deliberately does not pretend to upload files. */
@Composable fun StudentAssignment(repository:ErpDataRepository,id:String) {
 val student=ownStudent(repository);val assignments by repository.assignments.collectAsState()
 val work=assignments.firstOrNull{it.id==id&&student!=null&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade&&it.division==student.division}
 if(work==null){EmptyStateView("Assignment unavailable","This assignment is not available to your signed-in class.");return}
 var busy by remember(id){mutableStateOf(false)};var error by remember(id){mutableStateOf("")};var saved by remember(id){mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 val context=androidx.compose.ui.platform.LocalContext.current;val view=androidx.compose.ui.platform.LocalView.current;val preference=LocalStudentExperience.current
 LazyColumn(Modifier.fillMaxSize().testTag("student_assignment_detail"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.section)) {
  item{WorkspacePageHeader("Assignment")}
  item{StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
   Row(horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentIcon(StudentIcons.Document,StudentTone.BLUE);Column{Text(work.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("${work.subject} · ${work.classGrade}-${work.division}",style=MaterialTheme.typography.bodySmall)}}
   StudentBadge(if(work.isCompletedByStudent)"Submitted"else "Due ${work.dueDate}",if(work.isCompletedByStudent)StudentTone.MINT else StudentTone.ORANGE)
   Text("Instructions",style=MaterialTheme.typography.labelLarge);Text(work.instructions.ifBlank{"No additional instructions."})
   if(work.attachmentUrl.isNotBlank())SchoolDocumentButton(work.attachmentUrl,work.attachmentName.ifBlank{"Open attachment"})
   else Text(if(work.attachmentName.isBlank())"No attachment provided."else "${work.attachmentName} · Download link unavailable",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   HorizontalDivider()
   Text("Your submission",style=MaterialTheme.typography.titleSmall)
   val mode by repository.dataMode.collectAsState()
   if(mode==com.example.data.datasource.DataMode.SUPABASE_DEMO)StudentCloudSubmission(repository,id) else {
   Text("This school connection supports completion status. File and note submission is not available yet; no file will be uploaded.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
   if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
   androidx.compose.animation.AnimatedVisibility(saved,enter=androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(if(rememberReducedMotion())0 else StudentLayout.selectionMillis)),exit=androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(0))){Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.CheckCircle,null,tint=StudentColors.colors(StudentTone.MINT).second);Text("Completion status confirmed.",color=StudentColors.colors(StudentTone.MINT).second)}}
   StudentAction(if(busy)"Saving…"else if(work.isCompletedByStudent)"Mark as not submitted"else "Mark submitted",!busy){
    if(!busy){busy=true;error="";saved=false;scope.launch{try{repository.submitAssignmentConfirmed(id);saved=true;studentConfirmedFeedback(context,view,preference)}catch(cancelled:CancellationException){throw cancelled}catch(_:Exception){error="Status could not be saved. Refresh to confirm the current status before retrying."}finally{busy=false}}}
   }
   }
  }}}
 }
}
