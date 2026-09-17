package com.example.ui.screens.attendance

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalAttendanceScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit
) {
  val students by repository.students.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()

  var selectedClassSection by remember { mutableStateOf<String?>(null) } // Format: "Grade-Division"
  var selectedStudentForHistory by remember { mutableStateOf<Student?>(null) }

  val indiaTimeZone = remember { TimeZone.getTimeZone("Asia/Kolkata") }
  val currentDate = remember {
    val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a 'IST'", Locale("en", "IN"))
    sdf.timeZone = indiaTimeZone
    sdf.format(Date())
  }

  // LEVEL 1: Overall School Attendance calculation
  val schoolStats = remember(classAttendanceMap) {
    val records = classAttendanceMap.values
    val totalStudentsAcrossSessions = records.sumOf { it.totalStudents }
    val totalPresent = records.sumOf { it.presentCount }
    val totalAbsent = records.sumOf { it.absentCount }
    val totalLate = records.sumOf { it.lateCount }
    val totalLeave = records.sumOf { it.leaveCount }
    val totalSessions = records.size

    val percentage = if (totalStudentsAcrossSessions > 0) {
      ((totalPresent + totalLate).toDouble() / totalStudentsAcrossSessions) * 100.0
    } else {
      95.5 // baseline
    }

    object {
      val pct = percentage
      val present = totalPresent
      val absent = totalAbsent
      val late = totalLate
      val leave = totalLeave
      val sessions = totalSessions
    }
  }

  // LEVEL 2: Class-wise Attendance list (using actual records / baselines)
  val classList = remember(students, classAttendanceMap) {
    val sections = students.map { "${it.classGrade}-${it.division}" }.distinct().sorted()
    sections.map { sec ->
      val (grade, div) = sec.split("-")
      val classRecords = classAttendanceMap.values.filter { it.classGrade == grade && it.division == div }
      
      val (pct, present, absent, late) = if (classRecords.isNotEmpty()) {
        val totalSt = classRecords.sumOf { it.totalStudents }
        val pres = classRecords.sumOf { it.presentCount }
        val abs = classRecords.sumOf { it.absentCount }
        val lat = classRecords.sumOf { it.lateCount }
        val percentage = if (totalSt > 0) ((pres + lat).toDouble() / totalSt) * 100.0 else 100.0
        val formattedPct = String.format(Locale.US, "%.1f", percentage).toDouble()
        
        listOf(formattedPct, pres, abs, lat)
      } else {
        val classStudents = students.filter { it.classGrade == grade && it.division == div }
        val avgPct = if (classStudents.isNotEmpty()) classStudents.map { it.attendancePercent }.average() else 95.0
        val formattedPct = String.format(Locale.US, "%.1f", avgPct).toDouble()
        
        listOf(formattedPct, classStudents.size, 0, 0)
      }

      sec to object {
        val percentage = pct as Double
        val present = present as Int
        val absent = absent as Int
        val late = late as Int
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Attendance Hub", fontWeight = FontWeight.Bold)
            Text(
              text = if (selectedClassSection != null) "Class $selectedClassSection Analysis" else "Principal School-wide Overview",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = {
            if (selectedClassSection != null) {
              selectedClassSection = null
            } else {
              onBack()
            }
          }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (selectedClassSection == null) {
        // ================= LEVEL 1 & 2: OVERALL & CLASS LIST =================
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("principal_attendance_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // LEVEL 1: Overall Animated progress Ring and Stats Card
          item {
            AnimatedFadeIn(delayMillis = 0) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ScholaOnyxSurface)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "Overall School Attendance Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScholaOnyxText
                  )
                  Text(
                    text = "Synced: $currentDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScholaOnyxMuted
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  // Animated Circular Progress Indicator
                  Box(contentAlignment = Alignment.Center) {
                    VitalRing(
                      value = schoolStats.pct.toFloat(),
                      label = "Average Presence",
                      size = VitalRingSize.HERO,
                      gradientStart = if (schoolStats.pct >= 75.0) StatusSuccessText else StatusDangerText,
                      trackColor = ScholaOnyxBorder,
                      textColor = ScholaOnyxText
                    )
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  // Attendance count breakdown
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Present", style = MaterialTheme.typography.labelSmall, color = ScholaOnyxMuted)
                      Text("${schoolStats.present}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusSuccessText)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = ScholaOnyxBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Absent", style = MaterialTheme.typography.labelSmall, color = ScholaOnyxMuted)
                      Text("${schoolStats.absent}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusErrorText)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = ScholaOnyxBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Late", style = MaterialTheme.typography.labelSmall, color = ScholaOnyxMuted)
                      Text("${schoolStats.late}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RevenexGold)
                    }
                  }
                }
              }
            }
          }

          // LEVEL 2: Class-wise Attendance list header
          item {
            SectionHeader(title = "Class-wise Performance")
          }

          // LEVEL 2: Class List
          items(classList) { (classSection, stats) ->
            AnimatedFadeIn(delayMillis = 150) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedClassSection = classSection }
                  .testTag("class_item_$classSection"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Class Code Badge
                  Box(
                    modifier = Modifier
                      .size(50.dp)
                      .background(RevenexPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = classSection,
                      fontWeight = FontWeight.Bold,
                      color = RevenexBlue,
                      fontSize = 15.sp
                    )
                  }

                  Spacer(modifier = Modifier.width(16.dp))

                  // Class Name and stats preview
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Class $classSection Registry",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${stats.present} Present • ${stats.absent} Absent • ${stats.late} Late",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  // Class Percentage Badge
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "${stats.percentage}%",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (stats.percentage >= 75.0) StatusSuccessText else StatusErrorText
                    )
                    Text(
                      text = "View details",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // ================= LEVEL 3: CLASS DETAILS & STUDENT LIST =================
        val targetClassSec = selectedClassSection!!
        val (grade, div) = targetClassSec.split("-")
        
        val classStudents = remember(students, targetClassSec) {
          students.filter { it.classGrade == grade && it.division == div }
        }

        val classRecord = remember(classAttendanceMap, targetClassSec) {
          classAttendanceMap.values.firstOrNull { it.classGrade == grade && it.division == div }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("class_detail_attendance_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Class Summary Card
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Class $targetClassSec Summary",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("Total Students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${classStudents.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  }
                  Column {
                    Text("Today's Present", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${classRecord?.presentCount ?: classStudents.size}", color = StatusSuccessText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  }
                  Column {
                    Text("Today's Absent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${classRecord?.absentCount ?: 0}", color = StatusErrorText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  }
                }
              }
            }
          }

          item {
            SectionHeader(title = "Student-wise Attendance")
          }

          // LEVEL 3: Student Registry List
          items(classStudents) { student ->
            // Use actual attendance history or default attendance percent
            val computedPct = remember(classAttendanceMap, student.id) {
              repository.calculateStudentAttendancePercentage(student.id, student.attendancePercent)
            }

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedStudentForHistory = student }
                .testTag("student_att_${student.id}"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Roll Number Indicator
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "#${student.rollNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Adm: ${student.admissionNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "$computedPct%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (computedPct >= 75.0) StatusSuccessText else StatusErrorText
                  )
                  Text(
                    text = "Click for History",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    }

    // Modal Dialog: Student Attendance History Log
    selectedStudentForHistory?.let { student ->
      val history = remember(classAttendanceMap, student.id) {
        classAttendanceMap.values.mapNotNull { rec ->
          val found = rec.studentList.firstOrNull { it.studentId == student.id }
          if (found != null) rec.date to found.status else null
        }.sortedByDescending { it.first }
      }

      AlertDialog(
        onDismissRequest = { selectedStudentForHistory = null },
        title = {
          Column {
            Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("Attendance Log • Class ${student.fullClass}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        text = {
          Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
            if (history.isEmpty()) {
              Text(
                text = "No historical session logs recorded yet. Default baseline attendance: ${student.attendancePercent}%.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
              )
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(history) { (date, status) ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(date, fontWeight = FontWeight.Medium)
                    StatusBadge(status = status.label, type = "attendance")
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { selectedStudentForHistory = null }) {
            Text("Close")
          }
        }
      )
    }
  }
}
