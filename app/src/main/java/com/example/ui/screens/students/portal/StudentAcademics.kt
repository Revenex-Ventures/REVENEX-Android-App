package com.example.ui.screens.students.portal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

internal fun studentPublishedReports(student:Student?,reports:List<ReportCard>)=reports.filter{student!=null&&it.published&&it.studentId==student.id&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId}

@Composable fun StudentLearn(navigate:(String)->Unit) {
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Learn")}
  item{StudentRow("Assignments","Work, instructions and submission status",StudentIcons.Clipboard,StudentTone.ORANGE,{navigate(Screen.Homework.route)})}
  item{StudentRow("Results","Your published marks and reports",StudentIcons.Results,StudentTone.LAVENDER,{navigate(Screen.ReportCard.route)})}
  item{StudentRow("Library","School catalogue and availability",StudentIcons.Book,StudentTone.MINT,{navigate(Screen.Library.route)})}
  item{StudentRow("Resources","Class notes and documents",StudentIcons.Document,StudentTone.BLUE,{navigate(Screen.StudyMaterial.route)})}
  item{StudentRow("Examinations","Upcoming exam schedules",StudentIcons.Calendar,StudentTone.LAVENDER,{navigate(Screen.Exams.route)})}
 }
}

@Composable fun StudentResults(repository:ErpDataRepository,reportId:String?=null) {
 val student=ownStudent(repository);val reports by repository.reportCards.collectAsState()
 val published=studentPublishedReports(student,reports);var selected by rememberSaveable(student?.id){mutableStateOf(reportId.orEmpty())}
 val report=published.firstOrNull{it.id==selected}?:published.firstOrNull()
 val context=LocalContext.current;var feedback by remember{mutableStateOf("")}
 LazyColumn(Modifier.fillMaxSize().testTag("student_results"),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Results")}
  if(report==null)item{EmptyStateView("No published results","Draft marks remain private until your school publishes them.")}
  else{
   item{LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(published,key={it.id}){item->FilterChip(item.id==report.id,{selected=item.id},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=StudentColors.action,selectedLabelColor=StudentColors.onAction),label={Text("${item.term} · ${item.academicYear}")})}}}
   item{StudentCard(tone=StudentTone.ORANGE){Column(Modifier.padding(StudentLayout.gap)){StudentBadge("Published",StudentTone.MINT);Text("Overall",style=MaterialTheme.typography.bodyMedium);Text("${"%.0f".format(report.percentage)}%",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Text("${report.totalObtained} / ${report.totalMax} marks",style=MaterialTheme.typography.bodySmall)}}}
   items(report.scores){score->StudentCard{Row(Modifier.padding(StudentLayout.gap),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(StudentLayout.gap)){StudentIcon(studentSubjectIcon(score.subjectName),studentSubjectTone(score.subjectName));Text(score.subjectName,Modifier.weight(1f));Text("${score.obtainedMarks} / ${score.maxMarks}",style=MaterialTheme.typography.labelLarge)}}}
   item{if(report.principalRemark.isNotBlank())Text(report.principalRemark);StudentAction("View report"){
    val uri=student?.let{com.example.ui.screens.academics.exportPublishedStudentReport(context,it,report)}
    if(uri==null)feedback="Report could not be saved. Please try again." else runCatching{context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).setDataAndType(uri,"application/pdf").addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION))}.onFailure{feedback="Report saved. Install a PDF viewer to open it."}
   };if(feedback.isNotBlank())Text(feedback,style=MaterialTheme.typography.bodySmall)}
  }
 }
}

@Composable fun StudentResources(repository:ErpDataRepository) {
 val student=ownStudent(repository);val materials by repository.studyMaterials.collectAsState()
 val own=materials.filter{student!=null&&it.schoolId==student.schoolId&&it.classGrade==student.classGrade}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Resources")}
  if(own.isEmpty())item{EmptyStateView("No resources","Notes shared for your class will appear here.")}
  items(own,key={it.id}){material->StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text(material.title,style=MaterialTheme.typography.titleMedium);Text("${material.subject} · ${material.fileType}",style=MaterialTheme.typography.bodySmall);Text(material.description);if(material.attachmentUrl.isNotBlank())SchoolDocumentButton(material.attachmentUrl,material.attachmentName.ifBlank{"Open document"})else Text("Download link unavailable",style=MaterialTheme.typography.bodySmall)}}}
 }
}

@Composable fun StudentExaminations(repository:ErpDataRepository) {
 val student=ownStudent(repository);val schedules by repository.examSchedules.collectAsState()
 val own=schedules.filter{student!=null&&it.isPublished&&it.schoolId==student.schoolId&&it.sessionId==student.sessionId&&it.classGrade==student.classGrade}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(StudentLayout.margin),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){
  item{WorkspacePageHeader("Examinations")}
  if(own.isEmpty())item{EmptyStateView("No published examinations","Your class exam schedule will appear when published by school.")}
  items(own,key={it.id}){exam->StudentCard{Column(Modifier.padding(StudentLayout.gap),verticalArrangement=Arrangement.spacedBy(StudentLayout.gap)){Text(exam.title,style=MaterialTheme.typography.titleLarge);Text("${exam.term} · ${exam.startDate} – ${exam.endDate}",style=MaterialTheme.typography.bodySmall);exam.subjects.forEach{subject->HorizontalDivider();Text(subject.subjectName,style=MaterialTheme.typography.titleSmall);Text("${subject.date} · ${subject.time} · ${subject.room}",style=MaterialTheme.typography.bodySmall);Text("Maximum ${subject.maxMarks} · Passing ${subject.passingMarks}",style=MaterialTheme.typography.bodySmall)}}}}
 }
}
