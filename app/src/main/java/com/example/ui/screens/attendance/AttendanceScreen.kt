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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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

  val cohorts = students.map { "Grade ${it.classGrade}-${it.division}" }.distinct().sorted()
  var selectedCohort by remember(currentUser.id, initialClassGrade, initialDivision) {
    mutableStateOf(if (initialClassGrade != null && initialDivision != null) "Grade $initialClassGrade-$initialDivision" else "")
  }
  LaunchedEffect(cohorts) { if (selectedCohort !in cohorts) selectedCohort = cohorts.firstOrNull().orEmpty() }
  var saving by remember(currentUser.id) { mutableStateOf(false) }

  var selectedSessionSlot by remember { mutableStateOf("Morning Roll Call") }
  val sessionSlots = listOf("Morning Roll Call", "STEM Lab", "Senior Practicum")

  var showSuccessNotification by remember { mutableStateOf(false) }
  var saveError by remember { mutableStateOf<String?>(null) }

  val classStudents = remember(students, selectedCohort) {
    students.filter { "Grade ${it.classGrade}-${it.division}" == selectedCohort }
  }
  val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
  var selectedDate by remember(currentUser.id) { mutableStateOf(today) }
  var showDatePicker by remember { mutableStateOf(false) }
  val editingToday = selectedDate == today
  if (showDatePicker) {
    val picker = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = { showDatePicker = false },
      confirmButton = { TextButton(onClick = {
        picker.selectedDateMillis?.let { millis ->
          selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
          }.format(Date(millis))
          showSuccessNotification = false; saveError = null
        }
        showDatePicker = false
      }, enabled = picker.selectedDateMillis != null) { Text("View date") } },
      dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
    ) { DatePicker(state = picker) }
  }
  val savedRecord = classAttendanceMap.values.lastOrNull { record ->
    record.date == selectedDate && record.sessionSlot == selectedSessionSlot &&
      classStudents.firstOrNull()?.let { it.classGrade == record.classGrade && it.division == record.division } == true
  }
  val rollCallMap = remember(classStudents, selectedSessionSlot, savedRecord, selectedDate) {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      classStudents.filter { editingToday || savedRecord != null }.forEach { st ->
        put(st.id, savedRecord?.studentList?.firstOrNull { it.studentId == st.id }?.status ?: AttendanceStatus.PRESENT)
      }
    }
  }

  var studentQuery by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id, selectedCohort) { mutableStateOf("") }
  val visibleStudents = classStudents.filter { student ->
    studentQuery.isBlank() || student.name.contains(studentQuery.trim(), true) || student.rollNumber.toString() == studentQuery.trim() || student.admissionNumber.contains(studentQuery.trim(), true)
  }.sortedBy { it.rollNumber }
  val totalScholars = classStudents.size
  val presentCount = rollCallMap.values.count { it == AttendanceStatus.PRESENT }
  val lateCount = rollCallMap.values.count { it == AttendanceStatus.LATE }
  val absentCount = rollCallMap.values.count { it == AttendanceStatus.ABSENT }
  val excusedCount = rollCallMap.values.count { it == AttendanceStatus.EXCUSED }

  val livePresenceRate = if (totalScholars > 0) {
    ((presentCount + lateCount + excusedCount).toDouble() / totalScholars.toDouble()) * 100.0
  } else 0.0

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg))
          .navigationBarsPadding()
      ) {
        Column(modifier = Modifier.padding(Spacing.s4)) {
          saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
          if (showSuccessNotification) {
            Surface(
              shape = RoundedCornerShape(Radius.md),
              color = SemanticColors.successContainer,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.s2)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticColors.onSuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Roll call successfully recorded & verified for $selectedCohort ($selectedSessionSlot)!",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = SemanticColors.onSuccess
                )
              }
            }
          }

          AppButton(
            text = if (saving) "Saving attendance…" else "Save attendance · $presentCount present, $absentCount absent",
            onClick = {
              showSuccessNotification = false
              saveError = null
              val list = classStudents.map { st ->
                StudentAttendance(
                  studentId = st.id,
                  studentName = st.name,
                  rollNumber = st.rollNumber,
                  status = rollCallMap[st.id] ?: AttendanceStatus.PRESENT
                )
              }
              val cohort = classStudents.firstOrNull() ?: return@AppButton
              val grade = cohort.classGrade
              val div = cohort.division
              saving = true
              repository.recordRollCallSession(
                classGrade = grade,
                division = div,
                sessionSlot = selectedSessionSlot,
                studentList = list,
                onResult = { saved -> saving = false; showSuccessNotification = saved; saveError = if (saved) null else "Attendance was not confirmed. Check your connection and retry." }
              )
            },
            enabled = editingToday && !saving && classStudents.isNotEmpty() && currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER),
            containerColor = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.hero))
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
                  text = "ATTENDANCE REGISTER",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  style = MaterialTheme.typography.labelSmall,
                  letterSpacing = TypeTokens.trackingMicroLabel
                )
                Text(
                  text = selectedCohort,
                  color = MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold
                )
              }
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = SemanticColors.warningContainer
              ) {
                Text(
                  text = if (savedRecord != null) "Saved register" else if (editingToday) "Unsaved" else "No record",
                  color = SemanticColors.onWarning,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(Spacing.s4))

            OutlinedButton(onClick = { showDatePicker = true }, enabled = !saving) {
              Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(Spacing.sm8)); Text(selectedDate)
            }
            if (!editingToday) Text("History is view-only. Return to today's date to record attendance.",
              style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Cohort Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
              items(cohorts) { ch ->
                val isSelected = selectedCohort == ch
                Surface(
                  shape = RoundedCornerShape(Radius.pill),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
                    .clickable {
                      selectedCohort = ch
                      showSuccessNotification = false
                    }
                ) {
                  Text(
                    text = ch,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = 12.dp, vertical = 6.dp)
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
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .border(1.dp, if (isSelected) ScholaGold else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.sm))
                    .clickable {
                      selectedSessionSlot = slot
                      showSuccessNotification = false
                    }
                ) {
                  Text(
                    text = slot,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight().padding(horizontal = Spacing.md12, vertical = Spacing.sm8)
                  )
                }
              }
            }
          }
        }
      }

      // Counts scroll independently; the bulk action never squeezes their labels.
      item {
        Column(Modifier.fillMaxWidth()) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
            item { ScholaPillBadge(status = "$presentCount Present") }
            item { ScholaPillBadge(status = "$lateCount Late") }
            item { ScholaPillBadge(status = "$absentCount Absent") }
            item { ScholaPillBadge(status = "$excusedCount Excused") }
          }
          TextButton(modifier = Modifier.align(Alignment.End), enabled = editingToday && !saving &&
            currentUser.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN,UserRole.TEACHER), onClick = {
              classStudents.forEach { rollCallMap[it.id] = AttendanceStatus.PRESENT }
              showSuccessNotification = false
            }) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm8))
            Text("Mark all present")
          }
        }
      }

      item {
        OutlinedTextField(studentQuery, { studentQuery = it }, Modifier.fillMaxWidth().testTag("register_search"),
          label = { Text("Search name, roll or admission number") }, singleLine = true,
          leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(Radius.input))
        Text("${visibleStudents.size} of ${classStudents.size} students · Save applies to the entire class",
          Modifier.padding(top = Spacing.sm8), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      // 3. STUDENT ROLL CALL CARDS
      if (!editingToday && savedRecord == null) item {
        EmptyStateView(title = "No register loaded", message = "No saved attendance is available for this class, date and session.")
      }
      items(if (editingToday || savedRecord != null) visibleStudents else emptyList(), key = { it.id }) { student ->
        val currentStatus = rollCallMap[student.id] ?: AttendanceStatus.PRESENT
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Avatar(student.name, size = 36.dp, background = MaterialTheme.colorScheme.primaryContainer,
              textColor = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(Spacing.md12))
            Column(Modifier.weight(1f)) {
              Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Roll #${student.rollNumber} · ${student.admissionNumber}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
            listOf(AttendanceStatus.PRESENT to "P", AttendanceStatus.LATE to "L", AttendanceStatus.ABSENT to "A", AttendanceStatus.EXCUSED to "E").forEach { (status,label) ->
              FilterChip(colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer), selected = currentStatus == status,
                enabled = editingToday && !saving && currentUser.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN,UserRole.TEACHER),
                onClick = { rollCallMap[student.id] = status; showSuccessNotification = false },
                label = { Text(label) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                  .semantics { contentDescription = "${status.name.lowercase()} for ${student.name}" }
                  .testTag("btn_${status.name.lowercase()}_${student.rollNumber}"))
            }
          }
        }
      }
    }
  }
}
