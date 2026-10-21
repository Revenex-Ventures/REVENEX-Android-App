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
fun StudentDashboardScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit
) {
  val students by repository.students.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val cloudDemo = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO

  val currentStudent = remember(students, selectedStudentId) {
    if (selectedStudentId.isNotBlank()) {
      students.firstOrNull { it.id == selectedStudentId }
    } else {
      students.firstOrNull()
    }
  }

  if (currentStudent == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(Spacing.s4),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(Spacing.s4))
        Text("No Student Profile Found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Spacing.s2))
        Text(
          "Your account is not linked to an active student record. Enrollment is managed by the school administrator.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
    return
  }

  val todayDayOfWeek = remember {
    SimpleDateFormat("EEEE", Locale.US).format(Date())
  }

  val todayClasses = remember(timetable, currentStudent, todayDayOfWeek) {
    timetable
      .filter {
        it.classGrade == currentStudent.classGrade &&
        it.division == currentStudent.division &&
        it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true)
      }
      .sortedBy { it.periodNumber }
  }

  val todayName = remember { currentDayOfWeekName() }
  var nowMinutes by remember { mutableIntStateOf(currentMinutesOfDay()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(20_000)
      nowMinutes = currentMinutesOfDay()
    }
  }

  val activeAssignments = remember(assignments, currentStudent.classGrade, currentStudent.division) {
    assignments.filter { it.classGrade == currentStudent.classGrade && it.division == currentStudent.division }
  }

  val trackBorder = MaterialTheme.colorScheme.outlineVariant
  val listState = rememberLazyListState()

  // Hoisted so the hero intro plays once per VISIT to this tab (cold start or
  // switching back) but never replays while the hero item is scrolled out of
  // view and re-created.
  var heroIntroPlayed by remember { mutableStateOf(false) }

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("student_dashboard_list"),
    contentPadding = PaddingValues(bottom = 96.dp)
  ) {
    // 0. GREETING
    item {
      val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
      val greeting = remember(hour) {
        when {
          hour < 12 -> "Good Morning"
          hour < 17 -> "Good Afternoon"
          else -> "Good Evening"
        }
      }
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = Spacing.screenPadding)
          .padding(top = Spacing.s4, bottom = Spacing.s1)
      ) {
        Text(
          text = "$greeting, ${currentStudent.name.substringBefore(" ")}",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.s1))
        Text(
          text = "Small steps every day become big results — keep going.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 1. HERO STUDENT IDENTITY & ATTENDANCE RING
    item {
      AnimatedAttendanceHero(
        attendanceValue = currentStudent.attendancePercent.toFloat(),
        label = "Academic Attendance",
        avatarLabel = currentStudent.name,
        avatarUrl = currentStudent.avatarUrl,
        introHasPlayed = heroIntroPlayed,
        onIntroPassed = { heroIntroPlayed = true },
        modifier = Modifier
          .fillMaxWidth()
          .padding(Spacing.screenPadding),
        nameContent = {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .background(Color(0xFF10B981), CircleShape)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "SCHOLAR DESK",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = currentStudent.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Adm. #${currentStudent.admissionNumber} · Roll #${currentStudent.rollNumber}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        trailingContent = {
          Surface(
            shape = RoundedCornerShape(Radius.pill),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        },
        statsContent = {
          HeroStatStrip(
            stats = listOf(
              HeroStat("CLASS", currentStudent.fullClass, MaterialTheme.colorScheme.primary, Icons.Default.School, "SEC ${currentStudent.division}"),
              HeroStat("CLASS RANK", if(cloudDemo) "Not ranked" else "#${currentStudent.rank}", Color(0xFFF59E0B), Icons.Default.EmojiEvents, if(cloudDemo) "Not calculated" else "TOP 5%"),
              HeroStat("CUMULATIVE GPA", if(cloudDemo) "Unavailable" else "${currentStudent.gpa} / 10", Color(0xFF34D399), Icons.Default.TrendingUp, if(cloudDemo) "No GPA scheme configured" else "EXCELLENT")
            )
          )
        }
      )
    }

    // 3. TODAY'S CLASSES
    item {
      Spacer(modifier = Modifier.height(Spacing.s5))
      SectionHeader(
        title = "Today's Schedule ($todayDayOfWeek)",
        actionText = "Full Schedule",
        onActionClick = { onNavigateTo(Screen.Timetable.route) }
      )

      if (todayClasses.isEmpty()) {
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Text(
            text = "No classes scheduled for today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          // Continuous rail spanning the whole list (dots sit on top of it)
          Box(
            modifier = Modifier
              .matchParentSize()
              .drawBehind {
                val trackX = 79.dp.toPx()
                drawLine(
                  color = trackBorder,
                  start = Offset(trackX, 0f),
                  end = Offset(trackX, size.height),
                  strokeWidth = 2.dp.toPx(),
                  cap = StrokeCap.Square
                )
              }
          )
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s2)
          ) {
            todayClasses.forEach { slot ->
              ScheduleBlockRow(
                slot = slot,
                status = scheduleStatus(
                  isToday = todayName.equals(todayDayOfWeek, ignoreCase = true),
                  nowMinutes = nowMinutes,
                  slot = slot
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }

    // 4. PENDING ASSIGNMENTS
    item {
      Spacer(modifier = Modifier.height(Spacing.s5))
      SectionHeader(
        title = "Homework & Projects",
        actionText = "View All",
        onActionClick = { onNavigateTo(Screen.Homework.route) }
      )

      if (activeAssignments.isEmpty()) {
        AppCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding)
        ) {
          Text(
            text = "No pending homework assignments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenPadding),
          verticalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          activeAssignments.take(3).forEach { hw ->
            AppCard(
              modifier = Modifier.fillMaxWidth(),
              onClick = { onNavigateTo(Screen.Homework.route) }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = hw.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${hw.subject} • Due: ${hw.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                StatusBadge(status = "PENDING")
              }
            }
          }
        }
      }
    }
  }
}

private data class HeroStat(
  val label: String,
  val value: String,
  val tint: Color,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val badgeText: String? = null
)

@Composable
private fun HeroStatStrip(stats: List<HeroStat>) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
    stats.forEach { stat ->
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
        Text(stat.label.lowercase().replaceFirstChar { it.titlecase() }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall)
        Text(stat.value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        stat.badgeText?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) }
      }
    }
  }
}
