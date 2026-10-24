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
import androidx.compose.ui.semantics.selected
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
  val canEdit = currentUser.role in setOf(UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.TEACHER)
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
    attendanceDateMatches(record.date, selectedDate) && record.sessionSlot == selectedSessionSlot &&
      classStudents.firstOrNull()?.let { it.classGrade == record.classGrade && it.division == record.division } == true
  }
  val rollCallMap = remember(classStudents, selectedSessionSlot, savedRecord, selectedDate, canEdit) {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      classStudents.filter { (editingToday && canEdit) || savedRecord != null }.forEach { st ->
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

          if (canEdit) AppButton(
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
          ) else Text("Only saved school attendance appears here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
      item {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
          Text("Class attendance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
          Text(if (savedRecord != null) "Saved" else if (editingToday && canEdit) "Draft" else "No record",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          OutlinedButton(onClick = { showDatePicker = true }, enabled = !saving, modifier = Modifier.weight(1f), shape = RoundedCornerShape(Radius.sm), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.xs4)); Text(selectedDate)
          }
          var cohortMenu by remember { mutableStateOf(false) }
          Box(Modifier.weight(1f)) {
            OutlinedButton(onClick = { cohortMenu = true }, enabled = !saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.sm), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
              Text(selectedCohort.ifBlank { "Select class" }, Modifier.weight(1f))
              Icon(Icons.Default.ExpandMore,null,Modifier.size(18.dp))
            }
            DropdownMenu(expanded = cohortMenu, onDismissRequest = { cohortMenu = false }) {
              cohorts.forEach { cohort -> DropdownMenuItem(text = { Text(cohort) }, onClick = {
                selectedCohort = cohort; cohortMenu = false; showSuccessNotification = false
              }) }
            }
          }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          sessionSlots.forEach { slot ->
            SchoolFilterChip(selected = selectedSessionSlot == slot, onClick = {
              if (!saving) { selectedSessionSlot = slot; showSuccessNotification = false }
            }, label = { Text(when(slot) { "Morning Roll Call" -> "Morning"; "Senior Practicum" -> "Practicum"; else -> slot }) }, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
          }
        }
        if (!editingToday) Text("History is view-only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      // Counts scroll independently; the bulk action never squeezes their labels.
      item {
        Column(Modifier.fillMaxWidth()) {
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
            listOf("$presentCount Present", "$absentCount Absent", "$lateCount Late", "$excusedCount Excused").forEach { summary ->
              Surface(Modifier.weight(1f), shape = RoundedCornerShape(Radius.sm), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(summary, Modifier.padding(Spacing.sm8), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }
          if (canEdit) TextButton(modifier = Modifier.align(Alignment.End), enabled = editingToday && !saving &&
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
          leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(Radius.sm),
          colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
        Text(if (canEdit) "${visibleStudents.size} of ${classStudents.size} students · Save applies to the entire class" else "Saved records for the selected date and session",
          Modifier.padding(top = Spacing.sm8), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      // 3. STUDENT ROLL CALL CARDS
      if (savedRecord == null && (!editingToday || !canEdit)) item {
        EmptyStateView(title = "No register loaded", message = "No saved attendance is available for this class, date and session.")
      }
      items(if ((editingToday && canEdit) || savedRecord != null) visibleStudents else emptyList(), key = { it.id }) { student ->
        val currentStatus = rollCallMap[student.id] ?: AttendanceStatus.PRESENT
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.md), color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
          BoxWithConstraints(Modifier.padding(Spacing.sm8)) {
            val stacked = maxWidth < 320.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
            val identity: @Composable () -> Unit = {
              Column {
                Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Roll ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            val controls: @Composable () -> Unit = {
              Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
                listOf(AttendanceStatus.PRESENT to "P", AttendanceStatus.ABSENT to "A", AttendanceStatus.LATE to "L", AttendanceStatus.EXCUSED to "E").forEach { (status,label) ->
                  val selected = currentStatus == status
                  val container = when (status) {
                    AttendanceStatus.PRESENT -> SemanticColors.successContainer
                    AttendanceStatus.ABSENT -> SemanticColors.dangerContainer
                    else -> SemanticColors.warningContainer
                  }
                  Surface(onClick = { rollCallMap[student.id] = status; showSuccessNotification = false },
                    enabled = editingToday && !saving && canEdit,
                    color = if (selected) container else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(Radius.xs),
                    modifier = Modifier.size(48.dp).semantics {
                      contentDescription = "${status.name.lowercase()} for ${student.name}"
                      this.selected = selected
                    }
                      .testTag("btn_${status.name.lowercase()}_${student.rollNumber}")) {
                    Box(contentAlignment = Alignment.Center) { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                      color = if (selected) when (status) { AttendanceStatus.PRESENT -> SemanticColors.onSuccess; AttendanceStatus.ABSENT -> SemanticColors.onDanger; else -> SemanticColors.onWarning } else MaterialTheme.colorScheme.onSurfaceVariant) }
                  }
                }
              }
            }
            if (stacked) Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) { identity(); controls() }
            else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
              Box(Modifier.weight(1f)) { identity() }; controls()
            }
          }
        }
      }
      item { Text("P Present · A Absent · L Late · E Excused", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
  }
}
