package com.example.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun StudentDashboardScreen(repository: ErpDataRepository, onNavigateTo: (String) -> Unit) {
  val students by repository.students.collectAsState()
  val actor by repository.currentUser.collectAsState()
  val selected by repository.selectedStudentId.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val notices by repository.notices.collectAsState()
  val notifications by repository.notifications.collectAsState()
  val fees by repository.feeRecords.collectAsState()
  val events by repository.events.collectAsState()
  val cloud=repository.dataMode.collectAsState().value==com.example.data.datasource.DataMode.SUPABASE_DEMO
  val parent=actor.role==UserRole.PARENT
  val children=if(!parent || cloud) students else students.filter {
    it.id==actor.associatedStudentId || it.id in actor.associatedChildNames ||
      (actor.phone.isNotBlank() && it.parentPhone.filter(Char::isDigit).takeLast(10)==actor.phone.filter(Char::isDigit).takeLast(10))
  }
  val child=children.firstOrNull {it.id==selected} ?: children.firstOrNull()
  if(child==null) { EmptyStateView(title="No linked student",message="Ask your school administrator to link your account to an active student record.");return }
  val work=assignments.filter {it.classGrade==child.classGrade && it.division==child.division}
  val pending=work.filter {!it.isCompletedByStudent}
  val day=currentDayOfWeekName()
  var minutes by remember {mutableIntStateOf(currentMinutesOfDay())}
  LaunchedEffect(actor.id) {while(true){delay(20_000);minutes=currentMinutesOfDay()}}
  val slots=timetable.filter {it.classGrade==child.classGrade && it.division==child.division && it.dayOfWeek.equals(day,true)}.sortedBy {it.periodNumber}
  val next=slots.firstOrNull {scheduleStatus(true,minutes,it)!=ScheduleRowStatus.COMPLETED}
  val balance=fees.filter {it.studentId==child.id}.sumOf {it.pendingAmount}
  val amount=java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(java.math.BigDecimal(balance).movePointLeft(2))
  var choosing by remember(actor.id) {mutableStateOf(false)}
  LazyColumn(Modifier.fillMaxSize().testTag("student_dashboard_list"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    item {
      if(parent) {
        SchoolGroup {
          Row(Modifier.fillMaxWidth().padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
            Avatar(child.name,size=36.dp,background=MaterialTheme.colorScheme.primaryContainer,textColor=MaterialTheme.colorScheme.onPrimaryContainer)
            Column(Modifier.weight(1f).clickable {onNavigateTo(Screen.StudentDetail.createRoute(child.id))}) {
              Text(child.name,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
              Text("Class ${child.fullClass} · Roll ${child.rollNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
              IconButton(onClick={choosing=true},enabled=children.size>1){Icon(Icons.Default.ExpandMore,"Choose child")}
              DropdownMenu(choosing,{choosing=false}) {children.forEach { c -> DropdownMenuItem(text={Text(c.name)},onClick={repository.setSelectedStudentId(c.id);choosing=false})}}
            }
          }
        }
        Spacer(Modifier.height(Spacing.md12))
        Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          SchoolMetric("${child.attendancePercent.toInt()}%","Attendance",Icons.Default.FactCheck,Modifier.weight(1f),SchoolMetricTone.POSITIVE){onNavigateTo(Screen.Attendance.route)}
          SchoolMetric(pending.size.toString(),"Tasks due",Icons.Default.Assignment,Modifier.weight(1f)){onNavigateTo(Screen.Homework.route)}
          SchoolMetric(notices.size.toString(),"Notices",Icons.Default.Campaign,Modifier.weight(1f)){onNavigateTo(Screen.Notices.route)}
        }
      } else {
        Text("Welcome, ${child.name.substringBefore(" ")}",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        Text("Class ${child.fullClass} · Roll ${child.rollNumber}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.md12))
        SchoolGroup {SchoolRow("School assistant","",Icons.Default.AutoAwesome,{onNavigateTo(Screen.AiAssistant.route)})}
      }
    }
    if(parent) item { SchoolSection("Latest updates","View all",{onNavigateTo(Screen.Notifications.route)}) {SchoolGroup {
      if(notifications.isEmpty()) Text("No school updates yet.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      notifications.take(3).forEach {n -> SchoolRow(n.title,n.message,Icons.Default.Notifications,{onNavigateTo(Screen.Notifications.route)},shortSchoolTimestamp(n.timestamp));HorizontalDivider()}
    }} }
    else {
      item {SchoolSection("Next class","View timetable",{onNavigateTo(Screen.Timetable.route)}) {SchoolGroup {
        if(next==null) Text("No more classes scheduled today.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
        else SchoolRow(next.subject,"${next.startTime} – ${next.endTime} · ${next.roomNumber}",Icons.Default.School,{onNavigateTo(Screen.Timetable.route)})
      }}}
      item {Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        SchoolShortcut("Attendance",Icons.Default.FactCheck,Modifier.weight(1f)){onNavigateTo(Screen.Attendance.route)}
        SchoolShortcut("Timetable",Icons.Default.CalendarMonth,Modifier.weight(1f)){onNavigateTo(Screen.Timetable.route)}
        SchoolShortcut("Fees",Icons.Default.AccountBalanceWallet,Modifier.weight(1f).testTag("family_fee_accounts")){onNavigateTo(Screen.Fees.route)}
      }}
    }
    if(parent) item {SchoolSection("School fees","View all",{onNavigateTo(Screen.Fees.route)}) {SchoolGroup {
      SchoolRow(amount,"Outstanding",Icons.Default.CurrencyRupee,{onNavigateTo(Screen.Fees.route)},"View invoice",Modifier.testTag("family_fee_accounts"))
    }}}
    item {SchoolSection("Tasks due","View all",{onNavigateTo(Screen.Homework.route)}) {
      if(pending.isEmpty()) Text("No pending assignments.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      pending.take(3).forEach {hw -> SchoolTaskRow(hw.title,"${hw.subject} · Due ${hw.dueDate}",hw.subject,hw.submissionStatus){onNavigateTo(Screen.Homework.route)} }
    }}
    item {SchoolSection(if(parent) "Upcoming" else "Latest notice","View all",{onNavigateTo(Screen.Notices.route)}) {SchoolGroup {
      if(parent) {
        val upcoming=events.filter { dashboardEventIsUpcoming(it.date) }
        if(upcoming.isEmpty()) Text("No upcoming school events available.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
        upcoming.take(2).forEach { SchoolRow(it.title,"${it.date} · ${it.time} · ${it.location}",Icons.Default.CalendarMonth,{onNavigateTo(Screen.Notices.route)}) }
      } else {
        if(notices.isEmpty()) Text("No school notice available.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
        notices.take(1).forEach { SchoolRow(it.title,it.publishedDate,Icons.Default.Campaign,{onNavigateTo(Screen.Notices.route)}) }
      }
    }}}
    item {SchoolSection("Student information") {SchoolGroup {
      SchoolRow("Profile","Academic, contact and document details",Icons.Default.AccountCircle,{onNavigateTo(Screen.StudentDetail.createRoute(child.id))})
      SchoolRow("Resources","Notes and shared documents",Icons.Default.Folder,{onNavigateTo(Screen.StudyMaterial.route)})
    }}}
  }
}

internal fun dashboardEventIsUpcoming(value: String): Boolean {
  val formats=listOf(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE,
    java.time.format.DateTimeFormatter.ofPattern("dd MMM uuuu",Locale.US))
  val date=formats.firstNotNullOfOrNull {format -> runCatching {java.time.LocalDate.parse(value,format)}.getOrNull()} ?: return false
  return !date.isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")))
}
