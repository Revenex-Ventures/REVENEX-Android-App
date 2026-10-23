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
  val actor by repository.currentUser.collectAsState()
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

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .testTag("student_dashboard_list"),
    contentPadding = PaddingValues(bottom = Spacing.xl24)
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
          style = MaterialTheme.typography.headlineSmall,
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
      OutlinedButton(
        onClick = { onNavigateTo(Screen.Fees.route) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenPadding, vertical = Spacing.sm8)
          .testTag("family_fee_accounts"),
        shape = RoundedCornerShape(Radius.input)
      ) {
        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
        Spacer(Modifier.width(Spacing.sm8))
        Text("Fee accounts · invoices & receipts", modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null)
      }
    }
    item {
      Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.screenPadding), verticalArrangement = Arrangement.spacedBy(Spacing.md12)) {
        if (actor.role == UserRole.PARENT) {
          val children = if (cloudDemo) students else students.filter {
            it.id == actor.associatedStudentId || it.id in actor.associatedChildNames ||
              (actor.phone.isNotBlank() && it.parentPhone.filter(Char::isDigit).takeLast(10) == actor.phone.filter(Char::isDigit).takeLast(10))
          }
          if (children.size > 1) {
            var choosingChild by remember(actor.id) { mutableStateOf(false) }
            Box {
              OutlinedButton(onClick = { choosingChild = true }) { Text("Viewing ${currentStudent.name}"); Icon(Icons.Default.ExpandMore, null) }
              DropdownMenu(choosingChild, { choosingChild = false }) {
                children.forEach { child -> DropdownMenuItem(text = { Text("${child.name} · ${child.fullClass}") },
                  onClick = { repository.setSelectedStudentId(child.id); choosingChild = false }) }
              }
            }
          }
        }
        OutlinedCard(onClick = { onNavigateTo(Screen.ReportCard.route) },
          shape = RoundedCornerShape(Radius.md), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
          Row(Modifier.fillMaxWidth().padding(Spacing.md12), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
            Avatar(currentStudent.name, size = 40.dp, background = SemanticColors.peachContainer, textColor = SemanticColors.onPeach)
            Column(Modifier.weight(1f)) {
              Text(currentStudent.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              Text("Class ${currentStudent.fullClass} · Roll ${currentStudent.rollNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, "Open student profile")
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
          CompactStatTile("Attendance", "${currentStudent.attendancePercent.toInt()}%", Icons.Default.FactCheck,
            iconTint = SemanticColors.onSuccess, iconContainerColor = SemanticColors.successContainer,
            modifier = Modifier.weight(1f), onClick = { onNavigateTo(Screen.Attendance.route) })
          CompactStatTile("Tasks pending", activeAssignments.count { !it.isCompletedByStudent }.toString(), Icons.Default.Assignment,
            iconTint = SemanticColors.onLavender, iconContainerColor = SemanticColors.lavenderContainer,
            modifier = Modifier.weight(1f), onClick = { onNavigateTo(Screen.Homework.route) })
        }
      }
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

                StatusBadge(status = hw.submissionStatus)
              }
            }
          }
        }
      }
    }
  }
}

