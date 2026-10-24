package com.example.ui.screens.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// ACADEMIC SCHEDULE & GRADEBOOK WORKSPACE
// Segmented sub-tabs: "Timetable & Slots" â†” "Gradebook & GPA"
// ============================================================================
@Composable
fun TimetableScreen(repository: ErpDataRepository) {
  val timetable by repository.timetable.collectAsState()
  val scores by repository.gradebookEntries.collectAsState()
  val actor by repository.currentUser.collectAsState()
  val cloud=repository.dataMode.collectAsState().value==com.example.data.datasource.DataMode.SUPABASE_DEMO
  var tab by remember(actor.id){mutableIntStateOf(0)}
  val days=listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
  var day by remember(actor.id){mutableStateOf(currentDayOfWeekName())}
  var cohort by remember(actor.id){mutableStateOf("")}
  var choosing by remember {mutableStateOf(false)}
  val classes=timetable.map {"${it.classGrade}-${it.division}"}.distinct().sorted()
  val slots=timetable.filter {it.dayOfWeek.equals(day,true)&&(cohort.isBlank()||"${it.classGrade}-${it.division}"==cohort)}.sortedBy {it.periodNumber}
  Column(Modifier.fillMaxSize().testTag("timetable_screen")) {
    Box(Modifier.padding(horizontal=Spacing.screenPadding)) {WorkspacePageHeader("Timetable")}
    TabRow(selectedTabIndex=tab,containerColor=MaterialTheme.colorScheme.background) {listOf("Timetable","Scores").forEachIndexed {index,title->Tab(selected=tab==index,onClick={tab=index},text={Text(title)})}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      if(tab==0) {
        item {
          LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(days){value->SchoolFilterChip(day==value,{day=value},{Text(value.take(3))})}}
          Box {OutlinedButton(onClick={choosing=true},shape=RoundedCornerShape(Radius.sm)){Text(cohort.ifBlank {"All classes"});Icon(Icons.Default.ExpandMore,null)}
            DropdownMenu(choosing,{choosing=false}){DropdownMenuItem(text={Text("All classes")},onClick={cohort="";choosing=false});classes.forEach {value->DropdownMenuItem(text={Text(value)},onClick={cohort=value;choosing=false})}}
          }
          Text("$day · ${slots.size} scheduled periods",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if(slots.isEmpty()) item {EmptyStateView(title="No classes scheduled",message="No accessible timetable periods are available for this day and class.")}
        items(slots,key={it.id}) {slot->Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(Radius.sm)) {
          Row(Modifier.padding(Spacing.md12),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
            Column(Modifier.width(72.dp)){Text(slot.startTime,style=MaterialTheme.typography.labelLarge);Text(slot.endTime,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Circle,null,Modifier.size(Spacing.sm8),tint=MaterialTheme.colorScheme.primary);Box(Modifier.width(1.dp).height(Spacing.xxxl48).background(MaterialTheme.colorScheme.outlineVariant))}
            Column(Modifier.weight(1f)) {
              Text(slot.subject,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
              Text("Class ${slot.classGrade}-${slot.division} · ${slot.roomNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
              Text(slot.teacherName,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }}
      } else {
        item {Text("Published scores · Edit and publish in Results",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        if(scores.isEmpty()) item {EmptyStateView(title="No assessment scores",message="Published results appear when available to your account.")}
        items(scores,key={it.id}) {score->SchoolGroup {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Row(Modifier.fillMaxWidth()){Text(score.assessmentTitle,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall);Text("${score.score}/${score.maxScore}",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)}
          Text("${score.subject} · ${score.cohort} · ${score.date}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${score.instructorName} · Grade ${score.letterGrade}",style=MaterialTheme.typography.bodySmall)
          if(!cloud) Text("Local demo GPA: ${score.gpaPoint}",style=MaterialTheme.typography.bodySmall)
        }}}
      }
    }
  }
}

// Homework Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(repository: ErpDataRepository, onShowCreateAssignment: () -> Unit) {
  val assignments by repository.assignments.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val scope=rememberCoroutineScope()
  var subjectFilter by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) {mutableStateOf("")}
  var pendingOnly by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) {mutableStateOf(false)}
  var resources by remember(currentUser.id) {mutableStateOf(false)}
  var submitting by remember(currentUser.id) {mutableStateOf<String?>(null)}
  var submissionError by remember(currentUser.id) {mutableStateOf<String?>(null)}
  val subjects=assignments.map {it.subject}.distinct().sorted()
  val selectedStudent by repository.selectedStudentId.collectAsState()
  val students by repository.students.collectAsState()
  val child=students.firstOrNull {it.id==selectedStudent}
  val filtered=assignments.filter {
    (currentUser.role!=UserRole.PARENT || (child!=null && it.classGrade==child.classGrade && it.division==child.division)) &&
    (subjectFilter.isBlank()||it.subject==subjectFilter)&&(!pendingOnly||!it.isCompletedByStudent)
  }
  Column(Modifier.fillMaxSize()) {
    WorkspacePageHeader("Assignments",Modifier.padding(horizontal=Spacing.screenPadding)) {
      if(!resources && currentUser.role in setOf(UserRole.TEACHER,UserRole.PRINCIPAL,UserRole.ADMIN)) Button(onClick=onShowCreateAssignment,shape=RoundedCornerShape(Radius.sm)) {Icon(Icons.Default.Add,null);Text("Add")}
    }
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal=Spacing.screenPadding,vertical=Spacing.sm8)) {
      listOf("Tasks","Resources").forEachIndexed {index,label ->
        SegmentedButton(selected=resources==(index==1),onClick={resources=index==1},shape=SegmentedButtonDefaults.itemShape(index,2)){Text(label)}
      }
    }
    if(resources) StudyMaterialScreen(repository)
    else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      item {
        LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          item {SchoolFilterChip(subjectFilter.isBlank(),{subjectFilter=""},{Text("All")})}
          items(subjects) {subject -> SchoolFilterChip(subjectFilter==subject,{subjectFilter=subject},{Text(subject)})}
        }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
          Text("${filtered.size} tasks",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          SchoolFilterChip(pendingOnly,{pendingOnly=!pendingOnly},{Text("Pending only")})
        }
      }
      submissionError?.let {message -> item {Text(message,color=MaterialTheme.colorScheme.error)}}
      if(filtered.isEmpty()) item {EmptyStateView(title="No matching assignments",message="Change the subject or status filter to see other work.")}
      items(filtered,key={it.id}) {hw ->
        var expanded by remember(hw.id) {mutableStateOf(false)}
        val well=when {hw.subject.contains("math",true)->SemanticColors.lavenderContainer;hw.subject.contains("science",true)||hw.subject.contains("physics",true)->SemanticColors.successContainer;hw.subject.contains("english",true)->SemanticColors.blueContainer;else->SemanticColors.peachContainer}
        Surface(onClick={expanded=!expanded},modifier=Modifier.fillMaxWidth().testTag("assignment_details_${hw.id}"),shape=RoundedCornerShape(Radius.md),color=well) {
          Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
              Icon(Icons.Default.Description,null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.onSurface)
              Column(Modifier.weight(1f)) {
                Text(hw.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
                Text("${hw.subject} · Class ${hw.classGrade}-${hw.division}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Due ${hw.dueDate}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Box(Modifier.widthIn(max=120.dp)){ScholaPillBadge(status=hw.submissionStatus)}
              Icon(if(expanded)Icons.Default.ExpandLess else Icons.Default.ChevronRight,if(expanded)"Hide details" else "Show details",Modifier.size(18.dp))
            }
            androidx.compose.animation.AnimatedVisibility(expanded) {
              Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                HorizontalDivider()
                Text(hw.instructions,style=MaterialTheme.typography.bodyMedium)
                Text("${hw.teacherName} · Assigned ${hw.assignedDate} · ${hw.maxPoints} points",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if(hw.attachmentUrl.isNotBlank()) SchoolDocumentButton(hw.attachmentUrl,hw.attachmentName.ifBlank {"Open attachment"})
                if(currentUser.role==UserRole.STUDENT) {
            TextButton(enabled = submitting == null, onClick={
              submitting = hw.id; submissionError = null
              scope.launch {
                try { repository.submitAssignmentConfirmed(hw.id) }
                catch(e:Exception) {
                  if (e is kotlinx.coroutines.CancellationException) throw e
                  submissionError = "Submission could not be confirmed. Check the current status before retrying."
                } finally { submitting = null }
              }
            }) {
              Text(if(submitting == hw.id) "Saving…" else if(hw.isCompletedByStudent) "Undo submission" else "Mark submitted")
            }
                }
              }
            }
          }
        }
      }
    }
  }
}

// Study Material Screen
@Composable
fun StudyMaterialScreen(repository: ErpDataRepository) {
  val materials by repository.studyMaterials.collectAsState()
  var query by remember {mutableStateOf("")}
  val visible=materials.filter {query.isBlank()||it.title.contains(query,true)||it.subject.contains(query,true)||it.chapter.contains(query,true)}
  LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("Resources")
      OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search documents or subjects")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.sm))}
    if(visible.isEmpty()) item {EmptyStateView(title="No matching resources",message="Shared notes and documents appear here when available.")}
    items(visible,key={it.id}) {material->
      var expanded by remember(material.id){mutableStateOf(false)}
      SchoolGroup {
        SchoolRow(material.title,"${material.subject} · ${material.chapter}",Icons.Default.Description,{expanded=!expanded},material.fileType)
        androidx.compose.animation.AnimatedVisibility(expanded) {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Text(material.description,style=MaterialTheme.typography.bodyMedium)
          Text("${material.teacherName} · Class ${material.classGrade} · ${material.uploadDate} · ${material.fileSize}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          if(material.attachmentUrl.isNotBlank()) SchoolDocumentButton(material.attachmentUrl,material.attachmentName.ifBlank {"Open document"})
          else Text("Document attachment is not available.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }}
      }
    }
  }
}

@Composable
fun ExamsScreen(repository: ErpDataRepository) {
  val schedules by repository.examSchedules.collectAsState()
  LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("Examinations")
      Text("Schedules available to your account",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    if(schedules.isEmpty()) item {EmptyStateView(title="No examinations scheduled",message="Examination schedules appear when available to your account.")}
    items(schedules,key={it.id}) {exam->
      var expanded by remember(exam.id){mutableStateOf(false)}
      SchoolGroup {
        SchoolRow(exam.title,"Class ${exam.classGrade} · ${exam.startDate} – ${exam.endDate}",Icons.Default.CalendarMonth,{expanded=!expanded},if(exam.isPublished)"Published" else "Draft")
        androidx.compose.animation.AnimatedVisibility(expanded) {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
          Text(exam.term,style=MaterialTheme.typography.labelLarge)
          exam.subjects.forEach {subject->
            Column {Text(subject.subjectName,style=MaterialTheme.typography.titleSmall);Text("${subject.date} · ${subject.time} · ${subject.room}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Maximum ${subject.maxMarks} · Passing ${subject.passingMarks}",style=MaterialTheme.typography.bodySmall)}
            HorizontalDivider()
          }
        }}
      }
    }
  }
}
