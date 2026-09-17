package com.example.ui.screens.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// ============================================================================
// ACADEMIC SCHEDULE & GRADEBOOK WORKSPACE
// Segmented sub-tabs: "Timetable & Slots" â†” "Gradebook & GPA"
// ============================================================================
@Composable
fun TimetableScreen(
  repository: ErpDataRepository
) {
  val currentUser by repository.currentUser.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val gradebookEntries by repository.gradebookEntries.collectAsState()

  var selectedSubTab by remember { mutableStateOf(0) } // 0 = Timetable & Slots, 1 = Gradebook & GPA
  var selectedDay by remember { mutableStateOf("Monday") }
  val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

  val filteredSlots = remember(timetable, selectedDay) {
    timetable
      .filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(GlassBgTransparent)
      .testTag("timetable_screen"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    // 1. HERO SURFACE (Dark Onyx, 22dp corners)
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
          Text(
            text = "ACADEMICS & EVALUATION",
            color = ScholaOnyxMuted,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = TypeTokens.trackingMicroLabel
          )
          Text(
            text = if (selectedSubTab == 0) "Lecture Schedule & Slots" else "Scholar Gradebook & GPA",
            color = ScholaOnyxText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Term 2 Evaluative Cycle â€¢ Standard 45-Min Lecture Practicums",
            color = ScholaOnyxMuted,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp
          )
        }
      }
    }

    // 2. SEGMENTED SUB-TAB SWITCHER ("Timetable & Slots" â†” "Gradebook & GPA")
    item {
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = ScholaSurface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .border(1.dp, ScholaBorder, RoundedCornerShape(Radius.pill))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf("Timetable & Slots", "Gradebook & GPA").forEachIndexed { index, title ->
            val isSelected = selectedSubTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.pill))
                .background(if (isSelected) ScholaTerracotta else Color.Transparent)
                .clickable { selectedSubTab = index }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ScholaTextSecondary,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // 3. SUB-TAB CONTENT
    if (selectedSubTab == 0) {
      // TAB 1: TIMETABLE & SLOTS â€” VERTICAL TIMELINE
      item {
        // Day selector chips (Monday - Friday)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          items(days) { day ->
            val isSelected = selectedDay == day
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = if (isSelected) ScholaSlateNavy else ScholaSurface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) ScholaSlateNavy else ScholaBorder, RoundedCornerShape(Radius.pill))
                .clickable { selectedDay = day }
            ) {
              Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else ScholaTextPrimary,
                modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp)
              )
            }
          }
        }
      }

      if (filteredSlots.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Lectures Scheduled",
            message = "No timetable slots found for $selectedDay."
          )
        }
      } else {
        // Timeline card: clean flat container hosting a continuous live rail, with each period as its own card
        item {
          val isToday = selectedDay.equals(todayName, ignoreCase = true)

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(Radius.xl))
              .background(ScholaSurface)
              .border(0.8.dp, ScholaBorder, RoundedCornerShape(Radius.xl))
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              // â”€â”€ LIVE HEADER â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (isToday) {
                  ScheduleLiveClock(minutes = nowMinutes)
                } else {
                  Text(
                    text = "SCHEDULE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = TypeTokens.trackingMicroLabel,
                    color = ScholaTextSecondary
                  )
                }
                Text(
                  text = "${filteredSlots.size} LECTURES",
                  style = MaterialTheme.typography.labelSmall,
                  letterSpacing = TypeTokens.trackingMicroLabel,
                  color = ScholaMuted
                )
              }

              HorizontalDivider(color = ScholaBorder, thickness = 1.dp)

              filteredSlots.forEachIndexed { index, slot ->
                ScheduleTimelineRow(
                  slot = slot,
                  status = scheduleStatus(isToday, nowMinutes, slot),
                  showDivider = index < filteredSlots.lastIndex,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }
      }
    } else {
      // TAB 2: GRADEBOOK & GPA
      item {
        SectionHeader(title = "Published Midterm & Practicum Scores (${gradebookEntries.size})")
      }

      items(gradebookEntries, key = { it.id }) { entry ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = entry.assessmentTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ScholaTextPrimary
              )
              Text(
                text = "Scholar: ${entry.studentName} (${entry.cohort}) â€¢ ${entry.subject}",
                style = MaterialTheme.typography.bodySmall,
                color = ScholaTextSecondary,
                fontSize = 11.sp
              )
              Text(
                text = "Instructor: ${entry.instructorName} â€¢ Evaluated ${entry.date}",
                style = MaterialTheme.typography.labelSmall,
                color = ScholaMuted,
                fontSize = 10.sp
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${entry.score} / ${entry.maxScore}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = ScholaOnTerracottaContainer
                )
                Spacer(modifier = Modifier.width(6.dp))
                ScholaPillBadge(status = entry.letterGrade)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "GPA: ${entry.gpaPoint}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ScholaGoldText
              )
            }
          }
        }
      }
    }
  }
}

// Homework Screen
@Composable
fun HomeworkScreen(
  repository: ErpDataRepository,
  onShowCreateAssignment: () -> Unit
) {
  val assignments by repository.assignments.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(GlassBgTransparent),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Active Homework Tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL) {
          Button(onClick = onShowCreateAssignment, colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracotta)) {
            Text("Assign", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    if (assignments.isEmpty()) {
      item { EmptyStateView(title = "No Homework Assigned", message = "No pending assignments.") }
    } else {
      items(assignments, key = { it.id }) { hw ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(hw.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("${hw.subject} â€¢ Class ${hw.classGrade}-${hw.division} â€¢ Due ${hw.dueDate}", style = MaterialTheme.typography.bodySmall, color = ScholaMuted)
              Spacer(modifier = Modifier.height(4.dp))
              Text(hw.instructions, style = MaterialTheme.typography.bodyMedium, color = ScholaTextSecondary)
            }
            ScholaPillBadge(status = hw.submissionStatus)
          }
        }
      }
    }
  }
}

// Study Material Screen
@Composable
fun StudyMaterialScreen(repository: ErpDataRepository) {
  val materials by repository.studyMaterials.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(GlassBgTransparent),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      Text("Academic Study Materials", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }

    items(materials, key = { it.id }) { mat ->
      AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(mat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${mat.subject} â€¢ ${mat.chapter} â€¢ By ${mat.teacherName}", style = MaterialTheme.typography.bodySmall, color = ScholaMuted)
          }
          Surface(shape = RoundedCornerShape(Radius.pill), color = ScholaSlateContainer) {
            Text(mat.fileType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
          }
        }
      }
    }
  }
}

// Exams Screen
@Composable
fun ExamsScreen(repository: ErpDataRepository) {
  val schedules by repository.examSchedules.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(GlassBgTransparent),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      Text("Examination Schedules", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }

    schedules.forEach { sched ->
      item {
        SectionHeader(title = "${sched.title} (${sched.term})")
      }
      items(sched.subjects) { subj ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(subj.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Room: ${subj.room} â€¢ Max Marks: ${subj.maxMarks}", style = MaterialTheme.typography.bodySmall, color = ScholaMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(subj.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ScholaOnTerracottaContainer)
              Text(subj.time, style = MaterialTheme.typography.labelSmall, color = ScholaMuted, fontSize = 9.sp)
            }
          }
        }
      }
    }
  }
}
