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
import kotlinx.coroutines.launch

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
  val dataMode by repository.dataMode.collectAsState()

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
      .background(MaterialTheme.colorScheme.background)
      .testTag("timetable_screen"),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      WorkspaceHeading(
        if (selectedSubTab == 0) "Class timetable" else "Assessment scores",
        if (selectedSubTab == 0) "Choose a day to see teaching periods, rooms and subjects."
        else "Read-only scores available to your account. Drafts are restricted by school permissions."
      )
    }

    // 2. SEGMENTED SUB-TAB SWITCHER ("Timetable & Slots" â†” "Gradebook & GPA")
    item {
      Surface(
        shape = RoundedCornerShape(Radius.pill),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(Radius.pill))
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          listOf("Timetable", "Scores").forEachIndexed { index, title ->
            val isSelected = selectedSubTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.pill))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { selectedSubTab = index }
                .heightIn(min = 48.dp).padding(vertical = Spacing.sm8),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // 3. SUB-TAB CONTENT
    if (selectedSubTab == 0) {
      // TAB 1: TIMETABLE & SLOTS — VERTICAL TIMELINE
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
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
                .clickable { selectedDay = day }
            ) {
              Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = Spacing.s3, vertical = 6.dp)
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
              .background(MaterialTheme.colorScheme.surface)
              .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.xl))
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              // ── LIVE HEADER ──────────────────────────────────
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = "${filteredSlots.size} LECTURES",
                  style = MaterialTheme.typography.labelSmall,
                  letterSpacing = TypeTokens.trackingMicroLabel,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

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
        SectionHeader(horizontalPadding = 0.dp, title = "Accessible assessment scores (${gradebookEntries.size})")
      }

      if (gradebookEntries.isEmpty()) item {
        EmptyStateView(title = "No assessment scores", message = "No accessible scores are loaded. Published results will appear when your school makes them available.")
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
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Scholar: ${entry.studentName} (${entry.cohort}) · ${entry.subject}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
              Text(
                text = "Instructor: ${entry.instructorName} · Evaluated ${entry.date}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${entry.score} / ${entry.maxScore}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(6.dp))
                ScholaPillBadge(status = entry.letterGrade)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if(dataMode==com.example.data.datasource.DataMode.SUPABASE_DEMO) "GPA not calculated" else "GPA: ${entry.gpaPoint}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SemanticColors.onWarning
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
  val scope = rememberCoroutineScope()
  var subjectFilter by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf("") }
  var pendingOnly by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf(false) }
  val subjects = assignments.map { it.subject }.distinct().sorted()
  val filteredAssignments = assignments.filter { (subjectFilter.isBlank() || it.subject == subjectFilter) && (!pendingOnly || !it.isCompletedByStudent) }
  var submitting by remember(currentUser.id) { mutableStateOf<String?>(null) }
  var submissionError by remember(currentUser.id) { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Assignments", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL || currentUser.role == UserRole.ADMIN) {
          Button(onClick = onShowCreateAssignment, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Assign", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        item { SchoolFilterChip(subjectFilter.isBlank(), { subjectFilter = "" }, { Text("All subjects") }) }
        items(subjects) { subject -> SchoolFilterChip(subjectFilter == subject, { subjectFilter = subject }, { Text(subject) }) }
      }
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${filteredAssignments.size} assignments", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SchoolFilterChip(pendingOnly, { pendingOnly = !pendingOnly }, { Text("Pending only") })
      }
    }
    submissionError?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
    if (filteredAssignments.isEmpty()) {
      item { EmptyStateView(title = "No matching assignments", message = "Change the subject or pending filter to see other work.") }
    } else {
      items(filteredAssignments, key = { it.id }) { hw ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
            Surface(color = SemanticColors.lavenderContainer, shape = RoundedCornerShape(Radius.pill)) {
              Text(hw.subject, modifier = Modifier.padding(horizontal = Spacing.md12, vertical = Spacing.sm8),
                style = MaterialTheme.typography.labelMedium, color = SemanticColors.onLavender)
            }
            Spacer(Modifier.weight(1f))
            ScholaPillBadge(status = hw.submissionStatus)
          }
          Text(hw.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          var expanded by remember(hw.id) { mutableStateOf(false) }
          if (expanded) Text(hw.instructions, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          TextButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("assignment_details_${hw.id}")) {
            Text(if (expanded) "Hide instructions" else "View instructions")
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Due ${hw.dueDate} · Class ${hw.classGrade}-${hw.division}",
              style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          if(hw.attachmentUrl.isNotBlank()) SchoolDocumentButton(hw.attachmentUrl,hw.attachmentName.ifBlank { "Open attachment" })
          if(currentUser.role==UserRole.STUDENT) {
            TextButton(enabled = submitting == null, onClick={
              submitting = hw.id; submissionError = null
              scope.launch {
                try { repository.submitAssignmentConfirmed(hw.id) }
                catch(e:Exception) {
                  if (e is kotlinx.coroutines.CancellationException) throw e
                  submissionError = "Submission could not be confirmed. Check the current status before retrying."
                } finally { submitting = null }
              }
            }) {
              Text(if(submitting == hw.id) "Saving…" else if(hw.isCompletedByStudent) "Undo submission" else "Mark submitted")
            }
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
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      WorkspaceHeading("Study materials", "Subject resources and reference documents shared with your account.")
    }

    if (materials.isEmpty()) item {
      EmptyStateView(title = "No materials shared", message = "Your class documents will appear here when a teacher shares them.")
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
            Text("${mat.subject} · ${mat.chapter} · By ${mat.teacherName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Surface(shape = RoundedCornerShape(Radius.pill), color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(mat.fileType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
          }
        }
        if(mat.attachmentUrl.isNotBlank()) SchoolDocumentButton(mat.attachmentUrl,mat.attachmentName.ifBlank { "Open material" })
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
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = Spacing.s4, end = Spacing.s4, top = Spacing.s2, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
  ) {
    item {
      WorkspaceHeading("Examinations", "Dates, rooms and assessment details for your available schedules.")
    }

    if (schedules.isEmpty()) item {
      EmptyStateView(title = "No examinations scheduled", message = "No exam schedule is available for this account yet.")
    }
    schedules.forEach { sched ->
      item {
        SectionHeader(horizontalPadding = 0.dp, title = "${sched.title} (${sched.term})")
      }
      items(sched.subjects) { subj ->
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(Modifier.weight(1f)) {
              Text(subj.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Room: ${subj.room} · Max Marks: ${subj.maxMarks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(subj.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
              Text(subj.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}
