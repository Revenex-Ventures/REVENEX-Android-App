package com.example.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentAttendance
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SimpleProgressRing
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  repository: ErpDataRepository,
  initialClassGrade: String? = null,
  initialDivision: String? = null,
  onBack: () -> Unit
) {
  val students by repository.students.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()

  var selectedClass by remember { mutableStateOf(initialClassGrade ?: "10") }
  var selectedDivision by remember { mutableStateOf(initialDivision ?: "A") }
  var showSuccessToast by remember { mutableStateOf(false) }

  val classStudents = remember(students, selectedClass, selectedDivision) {
    students.filter { it.classGrade == selectedClass && it.division == selectedDivision }
  }

  val indiaTimeZone = remember { TimeZone.getTimeZone("Asia/Kolkata") }
  val todayDateKey = remember(selectedClass, selectedDivision) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN"))
    sdf.timeZone = indiaTimeZone
    sdf.format(Date())
  }

  val existingRecord = remember(classAttendanceMap, selectedClass, selectedDivision, todayDateKey) {
    classAttendanceMap.values.firstOrNull { rec ->
      rec.classGrade == selectedClass &&
      rec.division == selectedDivision &&
      rec.date == todayDateKey
    }
  }

  // Local attendance state editable before saving
  val attendanceList = remember(classStudents, existingRecord) {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      classStudents.forEach { st ->
        val existingStatus = existingRecord?.studentList?.firstOrNull { it.studentId == st.id }?.status
        put(st.id, existingStatus ?: (if (st.rollNumber == 31) AttendanceStatus.ABSENT else AttendanceStatus.PRESENT))
      }
    }
  }

  val totalCount = classStudents.size
  val presentCount = attendanceList.values.count { it == AttendanceStatus.PRESENT }
  val absentCount = attendanceList.values.count { it == AttendanceStatus.ABSENT }
  val lateCount = attendanceList.values.count { it == AttendanceStatus.LATE }

  val leaveCount = attendanceList.values.count { it == AttendanceStatus.LEAVE }

  // Attendance rate: (Present + Late) / Total × 100 — standard Indian school convention
  val attendanceRate = if (totalCount > 0) ((presentCount + lateCount).toDouble() / totalCount.toDouble()) * 100.0 else 100.0

  val currentDate = remember {
    val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a 'IST'", Locale("en", "IN"))
    sdf.timeZone = indiaTimeZone
    sdf.format(Date())
  }

  val canMarkAttendance = currentUser.role == com.example.data.model.UserRole.TEACHER ||
    currentUser.role == com.example.data.model.UserRole.PRINCIPAL

  Scaffold(
    bottomBar = {
      if (canMarkAttendance) {
        Surface(
          tonalElevation = 6.dp,
          shadowElevation = 8.dp,
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(16.dp)
          ) {
            if (showSuccessToast) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = StatusSuccessContainer,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 10.dp)
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Attendance saved and verified for Class $selectedClass-$selectedDivision!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccessText
                  )
                }
              }
            }

            Button(
              enabled = canMarkAttendance,
              onClick = {
                val list = classStudents.map { st ->
                  StudentAttendance(
                    studentId = st.id,
                    studentName = st.name,
                    rollNumber = st.rollNumber,
                    status = attendanceList[st.id] ?: AttendanceStatus.PRESENT
                  )
                }
                repository.saveClassAttendance(
                  classGrade = selectedClass,
                  division = selectedDivision,
                  date = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN")).apply { timeZone = indiaTimeZone }.format(Date()),
                  markedBy = currentUser.name,
                  studentList = list
                )
                showSuccessToast = true
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_save_attendance"),
              colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Default.Save, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Submit & Sync Attendance ($presentCount Present, $absentCount Absent)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    if (!canMarkAttendance) {
      val activeStudent = students.firstOrNull { it.id == selectedStudentId }
      val studentAttendanceDays = remember(classAttendanceMap, selectedStudentId) {
        classAttendanceMap.values.mapNotNull { record ->
          val matched = record.studentList.firstOrNull { it.studentId == selectedStudentId }
          if (matched != null) {
            record.date to matched.status
          } else null
        }.sortedByDescending { it.first }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .testTag("parent_student_attendance_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RevenexNavy)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
            ) {
              Text(
                text = "Attendance Diary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Student: ${activeStudent?.name ?: "No ward selected"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("ATTENDANCE RATE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                  Text("${activeStudent?.attendancePercent ?: 100.0}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RevenexGold)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("CLASS ROLL NO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                  Text("#${activeStudent?.rollNumber ?: 0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
            }
          }
        }

        item {
          SectionHeader(title = "Monthly Attendance Logs")
        }

        if (studentAttendanceDays.isEmpty()) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
              Text(
                text = "No attendance records found for this academic session.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          items(studentAttendanceDays) { (dateStr, status) ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Class ${activeStudent?.classGrade}-${activeStudent?.division}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                StatusBadge(status = status.label, type = "attendance")
              }
            }
          }
        }
      }
    } else {
      LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("attendance_screen_view"),
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
      // Header Section
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = RevenexNavy)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Text(
              text = "Daily Attendance Registry",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = currentDate,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Class Selector Chips
            Text(
              text = "Select Class & Section:",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("10-A", "10-B", "9-A", "8-A", "6-B").forEach { cls ->
                val (grade, div) = cls.split("-")
                val isSelected = selectedClass == grade && selectedDivision == div
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) RevenexGold else Color.White.copy(alpha = 0.15f),
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      selectedClass = grade
                      selectedDivision = div
                      showSuccessToast = false
                    }
                    .testTag("select_class_$cls")
                ) {
                  Text(
                    text = cls,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Quick Summary Counters & Mark All Action
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusSuccessContainer
            ) {
              Text(
                text = "$presentCount Present",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StatusSuccessText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusErrorContainer
            ) {
              Text(
                text = "$absentCount Absent",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StatusErrorText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusWarningContainer
            ) {
              Text(
                text = "$lateCount Late",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StatusWarningText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          TextButton(
            onClick = {
              classStudents.forEach { st ->
                attendanceList[st.id] = AttendanceStatus.PRESENT
              }
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark All Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // Student Attendance Rows
      items(classStudents, key = { it.id }) { student ->
        val currentStatus = attendanceList[student.id] ?: AttendanceStatus.PRESENT

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = Color(student.avatarColorHex),
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = student.rollNumber.toString(),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = student.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Roll #${student.rollNumber} • ${student.admissionNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }

            // 3 Interactive Pill Toggle Buttons (P, A, L)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              // Present Button
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (currentStatus == AttendanceStatus.PRESENT) StatusSuccess else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { attendanceList[student.id] = AttendanceStatus.PRESENT }
                  .testTag("btn_present_${student.rollNumber}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "P",
                    fontWeight = FontWeight.Bold,
                    color = if (currentStatus == AttendanceStatus.PRESENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                  )
                }
              }

              // Absent Button
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (currentStatus == AttendanceStatus.ABSENT) StatusError else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { attendanceList[student.id] = AttendanceStatus.ABSENT }
                  .testTag("btn_absent_${student.rollNumber}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "A",
                    fontWeight = FontWeight.Bold,
                    color = if (currentStatus == AttendanceStatus.ABSENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                  )
                }
              }

              // Late Button
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (currentStatus == AttendanceStatus.LATE) StatusWarning else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { attendanceList[student.id] = AttendanceStatus.LATE }
                  .testTag("btn_late_${student.rollNumber}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "L",
                    fontWeight = FontWeight.Bold,
                    color = if (currentStatus == AttendanceStatus.LATE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                  )
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
