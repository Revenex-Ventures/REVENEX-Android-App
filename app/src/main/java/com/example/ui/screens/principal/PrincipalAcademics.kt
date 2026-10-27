package com.example.ui.screens.principal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable fun PrincipalAcademics(repository:ErpDataRepository,navigate:(String)->Unit) {
 val principalDark=LocalPrincipalDark.current
 val cards by repository.reportCards.collectAsState()
 val students by repository.students.collectAsState()
 val mode by repository.dataMode.collectAsState()
 val sessions=students.map{it.sessionId}.distinct()
 val session=Regex("(20\\d{2})[_-](20\\d{2})").find(sessions.singleOrNull().orEmpty())
  ?.let{"${it.groupValues[1]}–${it.groupValues[2].takeLast(2)}"} ?: "Current session"
 val ready=cards.count{!it.published&&it.scores.any{score->mode!=com.example.data.datasource.DataMode.SUPABASE_DEMO||score.reviewState=="SUBMITTED"}}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_academics"),contentPadding=PaddingValues(if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
  item {PrincipalPageHeader("Academics"){Surface(shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.xs),color=MaterialTheme.colorScheme.surfaceVariant){Text(session,Modifier.padding(Spacing.sm8),style=MaterialTheme.typography.labelMedium)}}}
  if(principalDark)item {PrincipalSummary("ACADEMIC YEAR",session,"Loaded school session",PrincipalIcons.Academics,PrincipalColors.blue,PrincipalColors.blueInk)}
  item {PrincipalModules(listOf(
   PrincipalModule("Timetable",Icons.Outlined.CalendarMonth,Screen.Timetable.route,3),
   PrincipalModule("Subjects",PrincipalIcons.Academics,Screen.PrincipalSubjects.route,0),
   PrincipalModule("Assignments",Icons.Outlined.Assignment,Screen.Homework.route,3),
   PrincipalModule("Exams",Icons.Outlined.Description,Screen.Exams.route,0),
   PrincipalModule("Results",PrincipalIcons.Reports,Screen.ReportCard.route,2),
   PrincipalModule("Calendar",Icons.Outlined.CalendarToday,Screen.PrincipalCalendar.route,0)
  ),navigate,compact=true)}
  item {PrincipalHeading("For review") {
   if(ready>0)Surface(color=if(principalDark)PrincipalDarkPalette.surface else PrincipalColors.blue,shape=androidx.compose.foundation.shape.RoundedCornerShape(Radius.sm)){SchoolRow("Results ready ($ready)","Review submitted drafts before publication",Icons.Outlined.BarChart,{navigate(Screen.ReportCard.route)})}
   else Text("No submitted result drafts awaiting review.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  }}
 }
}

@Composable fun PrincipalSubjects(repository:ErpDataRepository) {
 val principalDark=LocalPrincipalDark.current
 val timetable by repository.timetable.collectAsState()
 val assignments by repository.assignments.collectAsState()
 val subjects=(timetable.map{it.subject}+assignments.map{it.subject}).filter{it.isNotBlank()}.distinct().sorted()
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Subjects");Text("From accessible timetable and assignment records",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(subjects.isEmpty())item {EmptyStateView("No subjects","No subject records are loaded for your account.")}
  items(subjects){subject->SchoolGroup{SchoolDetailRow(subject,"${timetable.count{it.subject==subject}} lessons")}}
 }
}
@Composable fun PrincipalCalendar(repository:ErpDataRepository) {
 val principalDark=LocalPrincipalDark.current
 val events by repository.events.collectAsState()
 var selected by remember {mutableStateOf<String?>(null)}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(if(principalDark)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("Calendar")}
  if(events.isEmpty())item {EmptyStateView("No events","No school events are available in the loaded records.")}
  items(events.sortedBy{it.date},key={it.id}) {event->PrincipalRecordRow(event.title,"${event.date} · ${event.time}",{selected=event.id})}
 }
 events.firstOrNull{it.id==selected}?.let {event->AlertDialog(onDismissRequest={selected=null},title={Text(event.title)},text={Column(Modifier.heightIn(max=PrincipalLayout.historyHeight).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){SchoolDetailRow("Date",event.date);SchoolDetailRow("Time",event.time);SchoolDetailRow("Location",event.location);Text(event.description)}},confirmButton={TextButton(onClick={selected=null}){Text("Done")}})}
}
