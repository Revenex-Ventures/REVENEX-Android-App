package com.example.ui.screens.principal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.datasource.DataMode
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.compactSchoolMoney
import com.example.ui.navigation.Screen
import com.example.ui.screens.attendance.parseSchoolRecordDate
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A distinct routed composition. Metrics describe loaded, authorized records only. */
@Composable internal fun PrincipalDarkHome(repository:ErpDataRepository,navigate:(String)->Unit) {
 val actor by repository.currentUser.collectAsState()
 val students by repository.students.collectAsState()
 val staff by repository.teachers.collectAsState()
 val registers by repository.classAttendance.collectAsState()
 val invoices by repository.invoices.collectAsState()
 val leaves by repository.leaveRequests.collectAsState()
 val cards by repository.reportCards.collectAsState()
 val notices by repository.notices.collectAsState()
 val mode by repository.dataMode.collectAsState()
 val latest=registers.values.mapNotNull{parseSchoolRecordDate(it.date)}.maxOrNull()
 val records=registers.values.filter{parseSchoolRecordDate(it.date)==latest}
 val total=records.sumOf{it.totalStudents}
 val present=records.sumOf{it.presentCount+it.lateCount+it.excusedCount}
 val attendance=if(total>0)String.format(Locale.US,"%.0f%%",present*100.0/total)else "—"
 val year=students.map{it.sessionId}.distinct().singleOrNull()?.let{session->Regex("(20\\d{2})[_-](20\\d{2})").find(session)?.let{"${it.groupValues[1]}–${it.groupValues[2].takeLast(2)}"}} ?: "Loaded session"
 val review=cards.count{!it.published&&it.scores.any{score->mode!=DataMode.SUPABASE_DEMO||score.reviewState=="SUBMITTED"}}
 val today=LocalDate.now(ZoneId.of("Asia/Kolkata"))
 val modules=listOf(
  PrincipalModule("Students",PrincipalIcons.Students,Screen.Students.route,0),
  PrincipalModule("Staff",Icons.Outlined.Person,Screen.Teachers.route,1),
  PrincipalModule("Attendance",Icons.Outlined.CalendarMonth,Screen.PrincipalAttendance.route,1),
  PrincipalModule("Academics",PrincipalIcons.Academics,Screen.Academics.route,0),
  PrincipalModule("Fees",Icons.Outlined.CreditCard,Screen.Fees.route,0),
  PrincipalModule("Reports",PrincipalIcons.Reports,Screen.Reports.route,0))
 LazyColumn(Modifier.fillMaxSize().testTag("principal_dashboard_list"),contentPadding=PaddingValues(Spacing.xl20),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
  item {Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   Text(today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM",Locale.getDefault())).uppercase(),style=MaterialTheme.typography.labelMedium,color=PrincipalDarkPalette.muted)
   Text("A clear view of\nyour school.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
   Text("${if(mode==DataMode.DEMO)"Local Demo school"else "School …${actor.schoolId.takeLast(6)}"} · $year",style=MaterialTheme.typography.bodyLarge,color=PrincipalDarkPalette.muted)
  }}
  item {Surface(color=PrincipalDarkPalette.orange,contentColor=PrincipalDarkPalette.ink,shape=RoundedCornerShape(Radius.hero)) {
   Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    Text(if(latest==today)"TODAY AT A GLANCE" else "LATEST SAVED ATTENDANCE",style=MaterialTheme.typography.labelMedium)
    Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(attendance,style=MaterialTheme.typography.displayMedium,fontWeight=FontWeight.Bold);Text("Student attendance",style=MaterialTheme.typography.titleMedium)};Icon(Icons.Outlined.CalendarMonth,null,Modifier.size(Spacing.xxxl48))}
    Text(latest?.let{"$it · $total saved entries"} ?: "No saved register available",style=MaterialTheme.typography.bodySmall)
    Surface(onClick={navigate(Screen.PrincipalAttendance.route)},color=PrincipalDarkPalette.ink,contentColor=PrincipalDarkPalette.text,shape=RoundedCornerShape(Radius.md)) {Row(Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48).padding(horizontal=Spacing.md12),verticalAlignment=Alignment.CenterVertically){Text("View attendance",Modifier.weight(1f));Icon(Icons.Outlined.NorthEast,null)}}
   }
  }}
  item {Row(horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
   PrincipalDarkStat(students.count{it.status=="ACTIVE"}.toString(),"Students",PrincipalIcons.Students,PrincipalDarkPalette.blue,Modifier.weight(1f)){navigate(Screen.Students.route)}
   PrincipalDarkStat(staff.size.toString(),"Staff",Icons.Outlined.Person,PrincipalDarkPalette.mint,Modifier.weight(1f)){navigate(Screen.Teachers.route)}
  }}
  item {PrincipalHeading("Your school"){PrincipalModules(modules,navigate)}}
  item {PrincipalHeading("Needs your review") {Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
   PrincipalDarkAction(leaves.count{it.status==LeaveStatus.PENDING}.toString(),"Leave requests",Icons.Outlined.Description){navigate(Screen.Leaves.route)}
   PrincipalDarkAction(review.toString(),"Result reviews",PrincipalIcons.Reports){navigate(Screen.ReportCard.route)}
  }}}
  item {Surface(onClick={navigate(Screen.Fees.route)},color=PrincipalDarkPalette.blue,contentColor=PrincipalDarkPalette.ink,shape=RoundedCornerShape(Radius.hero)) {
   Row(Modifier.fillMaxWidth().padding(Spacing.lg16),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    Icon(PrincipalIcons.Fees,null);Column(Modifier.weight(1f)){Text(compactSchoolMoney(invoices.sumOf{it.paidAmount}),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Collected · loaded session ledger",style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.ChevronRight,null)
   }
  }}
  item {PrincipalHeading("School notice") {
   val notice=notices.firstOrNull()
   PrincipalDarkAction("",notice?.title ?: "No notices available",Icons.Outlined.Campaign){navigate(Screen.Notices.route)}
  }}
  item {TextButton(onClick={navigate(Screen.OperationsHub.route)}){Text("All school modules");Icon(Icons.Outlined.ChevronRight,null)}}
 }
}

@Composable internal fun PrincipalDarkStat(value:String,label:String,icon:ImageVector,color:Color,modifier:Modifier=Modifier,onClick:()->Unit={}) {
 Surface(onClick=onClick,modifier=modifier,color=color,contentColor=PrincipalDarkPalette.ink,shape=RoundedCornerShape(Radius.hero)) {
  Row(Modifier.padding(Spacing.lg16),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(value,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodyMedium)};Icon(icon,null,Modifier.size(PrincipalLayout.moduleIcon))}
 }
}
@Composable internal fun PrincipalDarkAction(value:String,label:String,icon:ImageVector,onClick:()->Unit) {
 Surface(onClick=onClick,color=PrincipalDarkPalette.surface,shape=RoundedCornerShape(Radius.md)) {
  Row(Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48).padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
   Icon(icon,null,tint=PrincipalDarkPalette.muted)
   if(value.isNotBlank())Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
   Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Icon(Icons.Outlined.ChevronRight,null,tint=PrincipalDarkPalette.muted)
  }
 }
}
