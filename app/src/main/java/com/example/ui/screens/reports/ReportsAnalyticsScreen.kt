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
  val cohorts=students.groupBy {it.fullClass}.toSortedMap()
  LazyColumn(Modifier.fillMaxSize().testTag("reports_analytics_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    item {
      Text("School reports",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
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
      Text("Each row is one saved class/date/session register.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      SchoolGroup {
        if(registers.isEmpty()) Text("No saved registers available.",Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall)
        registers.values.sortedByDescending {it.date}.forEach {record ->
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
