package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.SimpleProgressRing
import com.example.ui.components.StatCard
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ReportsAnalyticsScreen(repository: ErpDataRepository) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val fees by repository.feeRecords.collectAsState()
  val registers by repository.classAttendance.collectAsState()
  val actor by repository.currentUser.collectAsState()
  val cloud=repository.dataMode.collectAsState().value==com.example.data.datasource.DataMode.SUPABASE_DEMO
  val money:(Long)->String={java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(java.math.BigDecimal(it).movePointLeft(2))}
  var attendancePeriod by remember {mutableStateOf("All records")}
  val today=java.time.LocalDate.now()
  val attendanceRows=registers.values.filter { record ->
    if(attendancePeriod=="All records") true else {
      val date=com.example.ui.screens.attendance.parseSchoolRecordDate(record.date)
      val start=if(attendancePeriod=="This week")today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) else today.withDayOfMonth(1)
      date!=null && !date.isBefore(start) && !date.isAfter(today)
    }
  }.sortedByDescending {com.example.ui.screens.attendance.parseSchoolRecordDate(it.date)}
  val cohorts=students.groupBy {it.fullClass}.toSortedMap()
  LazyColumn(Modifier.fillMaxSize().testTag("reports_analytics_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    item {
      WorkspacePageHeader("School reports")
      Text(if(cloud) "School ${actor.schoolId} · loaded academic session" else "Local sample school · loaded session",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      Text("Totals reflect records available to your account. No growth or trend values are estimated.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    item {Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
      SchoolMetric(students.size.toString(),"Students",Icons.Default.Groups,Modifier.weight(1f))
      SchoolMetric(teachers.size.toString(),"Faculty",Icons.Default.School,Modifier.weight(1f))
      SchoolMetric(cohorts.size.toString(),"Classes",Icons.Default.Class,Modifier.weight(1f))
    }}
    item {SchoolSection("Fee ledger · loaded session") {SchoolGroup {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      listOf("Billed" to fees.sumOf {it.totalFee},"Collected" to fees.sumOf {it.paidAmount},"Outstanding" to fees.sumOf {it.pendingAmount}).forEach {(label,value)->
        Row(Modifier.fillMaxWidth()) {Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Text(money(value),style=MaterialTheme.typography.titleSmall)}
      }
    }}}}
    item {SchoolSection("Enrolment by class") {
      if(cohorts.isEmpty()) EmptyStateView(title="No class data",message="No student records are available for this scope.")
      else SchoolGroup {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        val largest=cohorts.values.maxOf {it.size}.coerceAtLeast(1)
        cohorts.forEach {(name,pupils)->
          Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
            Text(name,Modifier.widthIn(min=64.dp),style=MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(progress={pupils.size.toFloat()/largest},modifier=Modifier.weight(1f),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.secondaryContainer)
            Text(pupils.size.toString(),style=MaterialTheme.typography.labelLarge)
          }
        }
      }}
    }}
    item {SchoolSection("Recorded attendance") {
      androidx.compose.foundation.lazy.LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        items(3) {index -> val period=listOf("All records","This week","This month")[index]
          SchoolFilterChip(attendancePeriod==period,{attendancePeriod=period},{Text(period)})
        }
      }
      Text("${attendanceRows.size} saved registers · $attendancePeriod",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      SchoolGroup {
        if(attendanceRows.isEmpty()) Text("No saved registers for this period.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
        attendanceRows.forEach {record ->
          Row(Modifier.fillMaxWidth().padding(Spacing.md12),horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            Column(Modifier.weight(1f)){Text("${record.classGrade}-${record.division} · ${record.date}",style=MaterialTheme.typography.titleSmall);Text(record.sessionSlot,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            Text("${record.presentCount}/${record.totalStudents} present",style=MaterialTheme.typography.labelMedium)
          }
          HorizontalDivider()
        }
      }
    }}
    item {SchoolSection("Faculty by department") {SchoolGroup {
      if(teachers.isEmpty()) Text("No faculty data available.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
      teachers.groupBy {it.department}.forEach {(department,staff)->Row(Modifier.fillMaxWidth().padding(Spacing.md12)) {Text(department,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Text(staff.size.toString(),style=MaterialTheme.typography.titleSmall)}}
    }}}
    item {Text("Report export is not configured in this demo.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }
}
