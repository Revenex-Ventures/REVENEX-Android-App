package com.example.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.screens.principal.*
import com.example.ui.screens.attendance.parseSchoolRecordDate
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrincipalDashboardScreen(repository:ErpDataRepository,onNavigateTo:(String)->Unit,
 onShowAddStudent:()->Unit,onShowAddTeacher:()->Unit,onShowCreateNotice:()->Unit) {
 if(LocalPrincipalDark.current){PrincipalDarkHome(repository,onNavigateTo);return}
 val actor by repository.currentUser.collectAsState()
 val students by repository.students.collectAsState()
 val invoices by repository.invoices.collectAsState()
 val records by repository.classAttendance.collectAsState()
 val leaves by repository.leaveRequests.collectAsState()
 val events by repository.events.collectAsState()
 val date=records.values.mapNotNull {parseSchoolRecordDate(it.date)}.maxOrNull()
 val latest=records.values.filter {parseSchoolRecordDate(it.date)==date}
 val total=latest.sumOf {it.totalStudents}
 val attendance=if(total>0)latest.sumOf {it.presentCount+it.lateCount+it.excusedCount}*100.0/total else null
 val hour=ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).hour
 val greeting=when(hour){in 5..11->"Good morning";in 12..16->"Good afternoon";else->"Good evening"}
 val name=actor.name.replace(Regex("^(Dr\\.?|Prof\\.?|Mr\\.?|Mrs\\.?|Ms\\.?)\\s+"),"").substringBefore(" ")
 val session=students.map {it.sessionId}.distinct().singleOrNull().orEmpty()
 val year=Regex("(20\\d{2})[_-](20\\d{2})").find(session)?.let {"${it.groupValues[1]}–${it.groupValues[2].takeLast(2)}"} ?: "Current session"
 var context by remember {mutableStateOf(false)}
 val shortcuts=listOf(
  PrincipalModule("Students",PrincipalIcons.Students,Screen.Students.route,0),
  PrincipalModule("Staff",PrincipalIcons.Staff,Screen.Teachers.route,2),
  PrincipalModule("Attendance",PrincipalIcons.Attendance,Screen.PrincipalAttendance.route,1),
  PrincipalModule("Academics",PrincipalIcons.Academics,Screen.Academics.route,3),
  PrincipalModule("Fees",PrincipalIcons.Fees,Screen.Fees.route,0),
  PrincipalModule("Reports",PrincipalIcons.Reports,Screen.Reports.route,2))
 LazyColumn(Modifier.fillMaxSize().testTag("principal_dashboard_list"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
  item {
   Text("$greeting, $name",style=MaterialTheme.typography.headlineSmall.copy(fontSize=PrincipalLayout.titleSize),fontWeight=FontWeight.Bold)
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    Text("Principal",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Surface(shape=RoundedCornerShape(Radius.sm),color=MaterialTheme.colorScheme.surfaceVariant){Row(Modifier.clickable{context=true}.padding(horizontal=Spacing.sm8,vertical=Spacing.xs4),verticalAlignment=Alignment.CenterVertically){Text(year,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant);Icon(Icons.Outlined.ExpandMore,null,Modifier.size(Spacing.lg16),tint=MaterialTheme.colorScheme.onSurfaceVariant)}}
   }
   Spacer(Modifier.height(Spacing.sm8))
   Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
    PrincipalMetric(students.count {it.status=="ACTIVE"}.toString(),"Students",PrincipalIcons.Students,PrincipalColors.blue,PrincipalColors.blueInk,Modifier.weight(1f)){onNavigateTo(Screen.Students.route)}
    PrincipalMetric(attendance?.let {String.format(Locale.US,"%.0f%%",it)} ?: "—","Attendance",Icons.Outlined.CheckCircle,PrincipalColors.mint,PrincipalColors.mintInk,Modifier.weight(1f)){onNavigateTo(Screen.PrincipalAttendance.route)}
    PrincipalMetric(compactSchoolMoney(invoices.sumOf {it.paidAmount}),"Collected",PrincipalIcons.Fees,PrincipalColors.peach,PrincipalColors.peachInk,Modifier.weight(1f)){onNavigateTo(Screen.Fees.route)}
   }
  }
  item {PrincipalHeading("School","View all",{onNavigateTo(Screen.OperationsHub.route)}){PrincipalModules(shortcuts,onNavigateTo)}}
  item {PrincipalHeading("Needs attention") {SchoolGroup {
   listOf(Triple("Approvals",leaves.count {it.status==LeaveStatus.PENDING},Screen.Leaves.route),Triple("Fee dues",invoices.count {it.pendingAmount>0},Screen.Fees.route)).forEachIndexed {i,(label,count,route)->
    Row(Modifier.fillMaxWidth().clickable{onNavigateTo(route)}.heightIn(min=PrincipalLayout.attentionRowHeight).testTag("principal_attention_$i").padding(horizontal=Spacing.lg16,vertical=Spacing.sm8),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
     Surface(shape=RoundedCornerShape(Radius.pill),color=if(i==0)MaterialTheme.colorScheme.primary else PrincipalColors.lavenderInk){Box(Modifier.size(Spacing.xxl32),contentAlignment=Alignment.Center){Text(count.toString(),color=MaterialTheme.colorScheme.surface,style=MaterialTheme.typography.titleMedium)}}
     Text(label,Modifier.weight(1f),fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.titleMedium)
     Icon(Icons.Outlined.ChevronRight,null,tint=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(i==0)HorizontalDivider(Modifier.padding(horizontal=Spacing.md12))
   }
  }}}
  item {PrincipalHeading("Upcoming") {
   val event=events.filter {dashboardEventIsUpcoming(it.date)}.minByOrNull {parseSchoolRecordDate(it.date) ?: LocalDate.MAX}
   if(event==null)SchoolGroup {Text("No upcoming events",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodyMedium)}
   else SchoolGroup {SchoolRow(event.title,"${event.date} · ${event.time}",Icons.Outlined.CalendarMonth,{onNavigateTo(Screen.PrincipalCalendar.route)})}
  }}
 }
 if(context)AlertDialog(onDismissRequest={context=false},title={Text("School context")},text={Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  SchoolDetailRow("School",actor.schoolId);SchoolDetailRow("Session",session.ifBlank {"Not available"})
  Text("Students: active records. Attendance: ${date ?: "no saved date"} · $total saved student entries. Collected: loaded session ledger.",style=MaterialTheme.typography.bodySmall)
 }},confirmButton={TextButton(onClick={context=false}){Text("Done")}})
}
