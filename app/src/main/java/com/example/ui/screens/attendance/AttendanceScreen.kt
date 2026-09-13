package com.example.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentAttendance
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
  val classAttendanceMap by repository.classAttendance.collectAsState()

  var selectedCohort by remember { mutableStateOf("Grade 10-A") }
  val cohorts = listOf("Grade 10-A", "Grade 10-B", "Grade 11-Sci", "Grade 12-Arts")

  var selectedSessionSlot by remember { mutableStateOf("Morning Roll Call") }
  val sessionSlots = listOf("Morning Roll Call", "STEM Lab", "Senior Practicum")

  var showSuccessNotification by remember { mutableStateOf(false) }

  val classStudents = remember(students, selectedCohort) {
    when (selectedCohort) {
      "Grade 10-A" -> students.filter { it.classGrade == "10" && it.division == "A" }
      "Grade 10-B" -> students.filter { it.classGrade == "10" && it.division == "B" }
      "Grade 11-Sci" -> students.filter { it.classGrade == "11" || it.fullClass.contains("11") }
      "Grade 12-Arts" -> students.filter { it.classGrade == "12" || it.fullClass.contains("12") }
      else -> students
    }
  }

  // Local editable roll call state
  val rollCallMap = remember(classStudents, selectedSessionSlot) {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      classStudents.forEach { st ->
        put(st.id, if (st.rollNumber == 31) AttendanceStatus.EXCUSED else AttendanceStatus.PRESENT)
      }
    }
  }

  val totalScholars = classStudents.size
  val presentCount = rollCallMap.values.count { it == AttendanceStatus.PRESENT }
  val lateCount = rollCallMap.values.count { it == AttendanceStatus.LATE }
  val absentCount = rollCallMap.values.count { it == AttendanceStatus.ABSENT }
  val excusedCount = rollCallMap.values.count { it == AttendanceStatus.EXCUSED }

  val livePresenceRate = if (totalScholars > 0) {
    ((presentCount + lateCount + excusedCount).toDouble() / totalScholars.toDouble()) * 100.0
  } else 100.0

  Scaffold(
    containerColor = ScholaLinen,
    bottomBar = {
      Surface(
        color = ScholaSurface,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, ScholaBorder, RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg))
          .navigationBarsPadding()
      ) {
        Column(modifier = Modifier.padding(Spacing.s4)) {
          if (showSuccessNotification) {
            Surface(
              shape = RoundedCornerShape(Radius.md),
              color = StatusSuccessBg,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.s2)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccessText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Roll call successfully recorded & verified for $selectedCohort ($selectedSessionSlot)!",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessText
                )
              }
            }
          }

          AppButton(
            text = "Dispatch Roll Call ($presentCount Present, $lateCount Late, $absentCount Absent, $excusedCount Excused)",
            onClick = {
              val list = classStudents.map { st ->
                StudentAttendance(
                  studentId = st.id,
                  studentName = st.name,
                  rollNumber = st.rollNumber,
                  status = rollCallMap[st.id] ?: AttendanceStatus.PRESENT
                )
              }
              val (grade, div) = when (selectedCohort) {
                "Grade 10-A" -> "10" to "A"
                "Grade 10-B" -> "10" to "B"
                "Grade 11-Sci" -> "11" to "Sci"
                else -> "12" to "Arts"
              }
              repository.recordRollCallSession(
                classGrade = grade,
                division = div,
                sessionSlot = selectedSessionSlot,
                studentList = list
              )
              showSuccessNotification = true
            },
            containerColor = ScholaTerracotta,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("submit_save_attendance")
          )
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
        .testTag("attendance_matrix_view"),
      verticalArrangement = Arrangement.spacedBy(Spacing.s3),
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
      // 1. HERO MATRIX SURFACE (Dark Onyx, 22dp corners)
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.hero))
            .background(ScholaOnyx)
            .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.hero))
            .padding(Spacing.cardPaddingLarge)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "ROLL CALL MATRIX",
                  color = ScholaOnyxMuted,
                  style = MaterialTheme.typography.labelSmall,
                  letterSpacing = TypeTokens.trackingMicroLabel
                )
                Text(
                  text = selectedCohort,
                  color = ScholaOnyxText,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold
                )
              }
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = ScholaGoldContainer
              ) {
                Text(
                  text = "Live Sync",
                  color = ScholaGoldText,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(Spacing.s4))

            // Cohort Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
              items(cohorts) { ch ->
                val isSelected = selectedCohort == ch
                Surface(
                  shape = RoundedCornerShape(Radius.pill),
                  color = if (isSelected) ScholaTerracotta else ScholaOnyxSurface,
                  modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .border(1.dp, if (isSelected) ScholaTerracotta else ScholaOnyxBorder, RoundedCornerShape(Radius.pill))
                    .clickable {
                      selectedCohort = ch
                      showSuccessNotification = false
                    }
                ) {
                  Text(
                    text = ch,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else ScholaOnyxMuted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(Spacing.s3))

            // Session Slot Switcher (Morning Roll Call, STEM Lab, Senior Practicum)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
              items(sessionSlots) { slot ->
                val isSelected = selectedSessionSlot == slot
                Surface(
                  shape = RoundedCornerShape(Radius.sm),
                  color = if (isSelected) ScholaSlateNavy else ScholaOnyxSurface,
                  modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .border(1.dp, if (isSelected) ScholaGold else ScholaOnyxBorder, RoundedCornerShape(Radius.sm))
                    .clickable {
                      selectedSessionSlot = slot
                      showSuccessNotification = false
                    }
                ) {
                  Text(
                    text = slot,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) ScholaGoldLight else ScholaOnyxMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 2. LIVE SUMMARY COUNTERS & MARK ALL PRESENT ACTION
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ScholaPillBadge(status = "$presentCount Present")
            ScholaPillBadge(status = "$lateCount Late")
            ScholaPillBadge(status = "$absentCount Absent")
            ScholaPillBadge(status = "$excusedCount Excused")
          }

          TextButton(
            onClick = {
              classStudents.forEach { st ->
                rollCallMap[st.id] = AttendanceStatus.PRESENT
              }
            },
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, tint = ScholaTerracotta, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark All", color = ScholaTerracotta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }

      // 3. STUDENT ROLL CALL CARDS
      items(classStudents, key = { it.id }) { student ->
        val currentStatus = rollCallMap[student.id] ?: AttendanceStatus.PRESENT

        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(ScholaSlateContainer, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = student.rollNumber.toString(),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = ScholaSlateNavy
                )
              }

              Spacer(modifier = Modifier.width(Spacing.s3))

              Column {
                Text(
                  text = student.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = ScholaTextPrimary
                )
                Text(
                  text = "Roll #${student.rollNumber} • Adm #${student.admissionNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = ScholaMuted,
                  fontSize = 11.sp
                )
              }
            }

            // 4 One-Tap Status Chips: Present, Late, Absent, Excused
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              listOf(
                AttendanceStatus.PRESENT to "P",
                AttendanceStatus.LATE to "L",
                AttendanceStatus.ABSENT to "A",
                AttendanceStatus.EXCUSED to "E"
              ).forEach { (status, label) ->
                val isActive = currentStatus == status
                val (bgColor, textColor) = when (status) {
                  AttendanceStatus.PRESENT -> StatusSuccessBg to StatusSuccessText
                  AttendanceStatus.LATE -> StatusWarningBg to StatusWarningText
                  AttendanceStatus.ABSENT -> StatusDangerBg to StatusDangerText
                  AttendanceStatus.EXCUSED -> StatusNeutralBg to StatusNeutralText
                  else -> ScholaSurface to ScholaMuted
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isActive) bgColor else ScholaSurface,
                  modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (isActive) textColor else ScholaBorder, RoundedCornerShape(8.dp))
                    .clickable { rollCallMap[student.id] = status }
                    .testTag("btn_${status.name.lowercase()}_${student.rollNumber}")
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = label,
                      fontWeight = FontWeight.Bold,
                      color = if (isActive) textColor else ScholaMuted,
                      fontSize = 12.sp
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
