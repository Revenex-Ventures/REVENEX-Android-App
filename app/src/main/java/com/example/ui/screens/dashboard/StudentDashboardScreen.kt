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

  val currentStudent = remember(students, selectedStudentId) {
    students.firstOrNull { it.id == selectedStudentId.ifBlank { "stu_1001" } }
      ?: students.firstOrNull()
  } ?: return

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

  val activeAssignments = remember(assignments, currentStudent.classGrade) {
    assignments.filter { it.classGrade == currentStudent.classGrade }
  }

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
          color = ScholaTextPrimary
        )
        Spacer(modifier = Modifier.height(Spacing.s1))
        Text(
          text = "Small steps every day become big results — keep going.",
          style = MaterialTheme.typography.bodySmall,
          color = ScholaMuted
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
                text = "SCHOLAR DESK",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = currentStudent.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Adm. #${currentStudent.admissionNumber}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
              )
              Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f)
              )
              Text(
                text = "Roll #${currentStudent.rollNumber}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = ScholaTerracottaLight,
                fontWeight = FontWeight.SemiBold
              )
            }
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
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = ScholaTerracottaLight,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "ACTIVE",
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
          HeroStatStrip(
            stats = listOf(
              HeroStat("CLASS", currentStudent.fullClass, ScholaTerracottaLight, Icons.Default.School, "SEC ${currentStudent.division}"),
              HeroStat("CLASS RANK", "#${currentStudent.rank}", Color(0xFFF59E0B), Icons.Default.EmojiEvents, "TOP 5%"),
              HeroStat("CUMULATIVE GPA", "${currentStudent.gpa} / 10", Color(0xFF34D399), Icons.Default.TrendingUp, "EXCELLENT")
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
                  color = ScholaBorder,
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
            if (stat.badgeText != null) {
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = stat.tint.copy(alpha = 0.16f)
              ) {
                Text(
                  text = stat.badgeText,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = stat.tint,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
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
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }
    }
  }
}
