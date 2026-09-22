package com.example.ui.screens.dashboard

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun TeacherDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowCreateAssignment: () -> Unit,
  onShowApplyLeave: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val classAttendanceMap by repository.classAttendance.collectAsState()

  val matchedTeacher = remember(teachers, currentUser) {
    teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) || it.name.equals(currentUser.name, ignoreCase = true) }
  }

  val mentorClass = matchedTeacher?.assignedClasses?.firstOrNull() ?: "10-A"
  val mentorClassGrade = mentorClass.substringBefore("-")
  val mentorClassDiv = mentorClass.substringAfter("-")

  val classStudents = remember(students, mentorClassGrade, mentorClassDiv) {
    students.filter { it.classGrade == mentorClassGrade && it.division == mentorClassDiv }
  }

  val todayDayOfWeek = remember {
    val sdf = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
    sdf.format(java.util.Date())
  }

  val todaySlots = remember(timetable, currentUser, todayDayOfWeek) {
    timetable.filter {
      it.teacherName.equals(currentUser.name, ignoreCase = true) &&
      it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true)
    }
  }

  val listState = rememberLazyListState()
  LaunchedEffect(key1 = Unit) {
    listState.scrollToItem(0)
  }

  // Hoisted so the hero intro plays once per VISIT to this tab (cold start or
  // switching back) but never replays while the hero item is scrolled out of
  // view and re-created.
  var heroIntroPlayed by remember { mutableStateOf(false) }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("teacher_dashboard_list"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Teacher Profile & Class Teacher Banner
    item {
      AnimatedFadeIn(delayMillis = 0) {
        val teacherAttendance = (matchedTeacher?.attendancePercent ?: 98.4).toFloat()
        AnimatedAttendanceHero(
          attendanceValue = teacherAttendance,
          label = "Term Attendance",
          avatarLabel = currentUser.name,
          introHasPlayed = heroIntroPlayed,
          onIntroPassed = { heroIntroPlayed = true },
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          nameContent = {
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(Radius.pill))
                  .background(Color.White.copy(alpha = 0.12f))
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFF10B981), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "FACULTY PORTAL",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  color = Color.White.copy(alpha = 0.9f),
                  letterSpacing = 1.1.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              val designation = matchedTeacher?.designation ?: currentUser.designation
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$designation • Class $mentorClass Mentor",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
              )
            }
          },
          trailingContent = {
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = ScholaTerracotta.copy(alpha = 0.18f),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, ScholaTerracotta.copy(alpha = 0.4f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = ScholaTerracottaLight,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "MENTOR",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = Color.White
                )
              }
            }
          },
          statsContent = {
            TeacherHeroStatStrip(
              stats = listOf(
                TeacherStat("MY CLASS", "$mentorClass (${classStudents.size} Std)", ScholaTerracottaLight, Icons.Default.Groups),
                TeacherStat("WEEKLY PERIODS", "${matchedTeacher?.weeklyPeriods ?: 22} Periods", Color(0xFF60A5FA), Icons.Default.Schedule),
                TeacherStat("ATTENDANCE", "$teacherAttendance%", Color(0xFF34D399), Icons.Default.FactCheck)
              )
            )
          }
        )
      }
    }

    // Quick Actions
    item {
      AnimatedFadeIn(delayMillis = 100) {
        Column {
          SectionHeader(title = "Faculty Quick Actions")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              QuickActionButton(
                title = "Mark Attendance",
                icon = Icons.Default.FactCheck,
                iconTint = ScholaSlateNavy,
                backgroundColor = ScholaSlateContainer,
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.Attendance.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Add Homework",
                icon = Icons.Default.Assignment,
                iconTint = ScholaTerracotta,
                backgroundColor = ScholaTerracottaContainer,
                modifier = Modifier.width(115.dp),
                onClick = onShowCreateAssignment
              )
            }
            item {
              QuickActionButton(
                title = "Upload Notes",
                icon = Icons.Default.CloudUpload,
                iconTint = ScholaSlateNavy,
                backgroundColor = ScholaSlateContainer,
                modifier = Modifier.width(115.dp),
                onClick = { onNavigateTo(Screen.StudyMaterial.route) }
              )
            }
            item {
              QuickActionButton(
                title = "Staff Leave",
                icon = Icons.Default.EventBusy,
                iconTint = ScholaSlateNavy,
                backgroundColor = ScholaSlateContainer,
                modifier = Modifier.width(115.dp),
                onClick = onShowApplyLeave
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }

    // Today's Teaching Schedule
    item {
      SectionHeader(
        title = "Today's Teaching Schedule ($todayDayOfWeek)",
        actionText = "Full Week",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        todaySlots.forEach { slot ->
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = RevenexPrimaryContainer,
                modifier = Modifier.size(48.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = "P${slot.periodNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = RevenexBlue
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = slot.subject,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${slot.startTime} - ${slot.endTime} • Class ${slot.classGrade}-${slot.division} (${slot.roomNumber})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StatusSuccessContainer
              ) {
                Text(
                  text = "Room ${slot.roomNumber.takeLast(3)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessText,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Class 10-A Student Overview & Attendance Quick Check
    item {
      SectionHeader(
        title = "Class 10-A Student Strength (${classStudents.size})",
        actionText = "Student Directory",
        onActionClick = { onNavigateTo(Screen.Students.route) }
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        classStudents.take(4).forEach { st ->
          AppCard(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateTo(Screen.StudentDetail.createRoute(st.id)) }
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = Color(st.avatarColorHex),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = st.rollNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = st.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Adm: ${st.admissionNumber} • Roll: #${st.rollNumber} • Rank #${st.rank}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StatusSuccessContainer
              ) {
                Text(
                  text = "${st.attendancePercent}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = StatusSuccessText,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}


private data class TeacherStat(
  val label: String,
  val value: String,
  val tint: Color,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun TeacherHeroStatStrip(stats: List<TeacherStat>) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    stats.forEach { stat ->
      Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
          width = 0.8.dp,
          brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.06f))
          )
        )
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.Start
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .background(Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = stat.icon,
              contentDescription = null,
              tint = stat.tint,
              modifier = Modifier.size(13.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = stat.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.65f),
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stat.value,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }
    }
  }
}
