package com.example.ui.screens.academics

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
import com.example.data.model.HomeworkAssignment
import com.example.data.model.StudyMaterial
import com.example.data.model.TimetableSlot
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun TimetableScreen(
  repository: ErpDataRepository
) {
  val currentUser by repository.currentUser.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val students by repository.students.collectAsState()
  val timetable by repository.timetable.collectAsState()
  var selectedDay by remember { mutableStateOf("Monday") }

  val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

  val activeStudent = remember(students, selectedStudentId, currentUser) {
    if (currentUser.role == UserRole.PARENT || currentUser.role == UserRole.STUDENT) {
      students.firstOrNull { it.id == selectedStudentId }
    } else null
  }

  val scopedSlots = remember(timetable, currentUser, activeStudent) {
    when (currentUser.role) {
      UserRole.PARENT, UserRole.STUDENT -> {
        if (activeStudent != null) {
          timetable.filter {
            it.classGrade.equals(activeStudent.classGrade, ignoreCase = true) &&
            it.division.equals(activeStudent.division, ignoreCase = true)
          }
        } else emptyList()
      }
      UserRole.TEACHER -> {
        timetable.filter { it.teacherName.equals(currentUser.name, ignoreCase = true) }
      }
      UserRole.PRINCIPAL -> timetable
    }
  }

  val filteredSlots = scopedSlots.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("timetable_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
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
            text = "Class Timetable & Schedule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          val subtitleText = when (currentUser.role) {
            UserRole.PARENT, UserRole.STUDENT -> {
              if (activeStudent != null) "Class ${activeStudent.classGrade}-${activeStudent.division} • 45 Minute Periods • CBSE Standard"
              else "No ward selected"
            }
            UserRole.TEACHER -> "Teacher Schedule • ${currentUser.name}"
            UserRole.PRINCIPAL -> "All Classes Schedule • School Overview"
          }
          Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
          )

          Spacer(modifier = Modifier.height(14.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(days) { day ->
              val isSelected = selectedDay == day
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) RevenexGold else Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedDay = day }
              ) {
                Text(
                  text = day.take(3),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }
          }
        }
      }
    }

    if (filteredSlots.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.CalendarToday,
          title = "Weekend / No Scheduled Classes",
          description = "No lecture periods assigned for $selectedDay."
        )
      }
    } else {
      items(filteredSlots, key = { it.id }) { slot ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = RevenexPrimaryContainer,
              modifier = Modifier.size(50.dp)
            ) {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "P${slot.periodNumber}",
                  style = MaterialTheme.typography.titleMedium,
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
                text = "${slot.startTime} - ${slot.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "${slot.teacherName} • ${slot.roomNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = slot.roomNumber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun HomeworkScreen(
  repository: ErpDataRepository,
  onShowCreateAssignment: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val selectedStudentId by repository.selectedStudentId.collectAsState()
  val students by repository.students.collectAsState()
  val assignments by repository.assignments.collectAsState()
  var selectedSubjectFilter by remember { mutableStateOf("All") }

  val activeStudent = remember(students, selectedStudentId, currentUser) {
    if (currentUser.role == UserRole.PARENT || currentUser.role == UserRole.STUDENT) {
      students.firstOrNull { it.id == selectedStudentId }
    } else null
  }

  val scopedAssignments = remember(assignments, currentUser, activeStudent) {
    when (currentUser.role) {
      UserRole.PARENT, UserRole.STUDENT -> {
        if (activeStudent != null) {
          assignments.filter {
            it.classGrade.equals(activeStudent.classGrade, ignoreCase = true) &&
            it.division.equals(activeStudent.division, ignoreCase = true)
          }
        } else emptyList()
      }
      UserRole.TEACHER -> {
        // Teachers only see assignments they made or for their department/subject/classes
        assignments.filter { it.teacherName.equals(currentUser.name, ignoreCase = true) }
      }
      UserRole.PRINCIPAL -> assignments
    }
  }

  val subjects = remember(scopedAssignments) {
    listOf("All") + scopedAssignments.map { it.subject }.distinct()
  }

  val filteredAssignments = remember(scopedAssignments, selectedSubjectFilter) {
    if (selectedSubjectFilter == "All") scopedAssignments
    else scopedAssignments.filter { it.subject == selectedSubjectFilter }
  }

  Scaffold(
    floatingActionButton = {
      if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL) {
        ExtendedFloatingActionButton(
          onClick = onShowCreateAssignment,
          icon = { Icon(Icons.Default.Add, contentDescription = null) },
          text = { Text("Assign HW", fontWeight = FontWeight.Bold) },
          containerColor = RevenexBlue,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_create_assignment")
        )
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("homework_screen_list"),
      contentPadding = PaddingValues(bottom = 100.dp)
    ) {
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(subjects) { subj ->
            FilterChip(
              selected = selectedSubjectFilter == subj,
              onClick = { selectedSubjectFilter = subj },
              label = { Text(subj) }
            )
          }
        }
      }

      items(filteredAssignments, key = { it.id }) { hw ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = RevenexPrimaryContainer
              ) {
                Text(
                  text = hw.subject,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = RevenexBlue,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
              StatusBadge(status = hw.submissionStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = hw.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = hw.instructions,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hw.attachmentName.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              var isDownloading by remember { mutableStateOf(false) }
              var downloadProgress by remember { mutableStateOf(0f) }
              var isDownloaded by remember { mutableStateOf(false) }
              var showDocViewer by remember { mutableStateOf(false) }

              LaunchedEffect(isDownloading) {
                if (isDownloading) {
                  for (i in 1..10) {
                    kotlinx.coroutines.delay(100)
                    downloadProgress = i / 10f
                  }
                  isDownloading = false
                  isDownloaded = true
                  showDocViewer = true
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = if (hw.attachmentName.endsWith(".pdf")) Icons.Default.Description else Icons.Default.Description,
                      contentDescription = null,
                      tint = if (hw.attachmentName.endsWith(".pdf")) Color(0xFFDC2626) else Color(0xFF2563EB),
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(
                        text = hw.attachmentName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = if (hw.attachmentName.endsWith(".pdf")) "PDF Reference • 1.2 MB" else "Reference doc • 450 KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  if (isDownloading) {
                    CircularProgressIndicator(
                      progress = { downloadProgress },
                      modifier = Modifier.size(24.dp),
                      strokeWidth = 2.dp
                    )
                  } else {
                    IconButton(onClick = {
                      if (isDownloaded) {
                        showDocViewer = true
                      } else {
                        isDownloading = true
                      }
                    }) {
                      Icon(
                        imageVector = if (isDownloaded) Icons.Default.Visibility else Icons.Default.Download,
                        contentDescription = if (isDownloaded) "Open File" else "Download File",
                        tint = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }

              if (showDocViewer) {
                AlertDialog(
                  onDismissRequest = { showDocViewer = false },
                  title = { Text(hw.attachmentName, fontWeight = FontWeight.Bold) },
                  text = {
                    Column {
                      Text(
                        text = "Viewing attached document content for assignment: ${hw.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(150.dp)
                          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                          .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                          .padding(12.dp)
                      ) {
                        Text(
                          text = "--- [MOCK DOCUMENT CONTENT] ---\n1. Read textbook section 4.2 to 4.5.\n2. Complete exercises listed on page 112.\n3. Show all working steps clearly.\n4. Upload completed scan as PDF.",
                          style = MaterialTheme.typography.bodySmall
                        )
                      }
                    }
                  },
                  confirmButton = {
                    TextButton(onClick = { showDocViewer = false }) {
                      Text("Close")
                    }
                  }
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Due: ${hw.dueDate} • By ${hw.teacherName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (currentUser.role == UserRole.STUDENT || currentUser.role == UserRole.PARENT) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = hw.isCompletedByStudent,
                    onCheckedChange = { repository.toggleAssignmentCompletion(hw.id) }
                  )
                  Text(
                    text = if (hw.isCompletedByStudent) "Done" else "Mark Done",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
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

@Composable
fun StudyMaterialScreen(
  repository: ErpDataRepository
) {
  val studyMaterials by repository.studyMaterials.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("study_materials_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(studyMaterials, key = { it.id }) { material ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = null,
              tint = Color(0xFF6D28D9),
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = material.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${material.subject} • ${material.chapter}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "${material.fileSize} • Uploaded by ${material.teacherName}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }

          IconButton(
            onClick = { /* Open file action */ }
          ) {
            Icon(Icons.Default.Download, contentDescription = "Download Notes", tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }
  }
}

@Composable
fun ExamsScreen(
  repository: ErpDataRepository
) {
  val examSchedules by repository.examSchedules.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()

  var activeSubjectForMarks by remember { mutableStateOf<Pair<com.example.data.model.ExamSchedule, com.example.data.model.ExamSubject>?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("exams_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RevenexNavy)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              tint = RevenexGoldLight,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = "Examination Schedules",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "CBSE & Term Assessment Timetables",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }
      }
    }

    if (examSchedules.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.EventBusy,
          title = "No Exams Scheduled",
          description = "There are currently no active or published examination timetables."
        )
      }
    } else {
      items(examSchedules, key = { it.id }) { exam ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = exam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              StatusBadge(
                status = if (exam.isPublished) "APPROVED" else "PENDING"
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Class ${exam.classGrade} • ${exam.term} (${exam.startDate} - ${exam.endDate})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Subject Schedule:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            exam.subjects.forEach { subject ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "${subject.date} • ${subject.time} (${subject.room})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                  ) {
                    Text(
                      text = "${subject.maxMarks} Marks",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                      onClick = { activeSubjectForMarks = Pair(exam, subject) },
                      modifier = Modifier.size(24.dp).testTag("enter_marks_${subject.subjectName}")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Enter Marks",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
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

  activeSubjectForMarks?.let { (exam, subject) ->
    var selectedStudent by remember { mutableStateOf<com.example.data.model.Student?>(null) }
    var obtainedMarksStr by remember { mutableStateOf("") }
    var showStudentDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    val classStudents = remember(students, exam.classGrade) {
      students.filter { it.classGrade == exam.classGrade }
    }

    AlertDialog(
      onDismissRequest = { activeSubjectForMarks = null },
      title = { Text("Enter Marks: ${subject.subjectName}", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Exam: ${exam.title} • Class ${exam.classGrade}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Student Selector
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = selectedStudent?.name ?: "Select Student",
              onValueChange = {},
              readOnly = true,
              label = { Text("Student") },
              trailingIcon = {
                IconButton(onClick = { showStudentDropdown = true }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
              },
              modifier = Modifier.fillMaxWidth().clickable { showStudentDropdown = true }
            )
            DropdownMenu(
              expanded = showStudentDropdown,
              onDismissRequest = { showStudentDropdown = false }
            ) {
              classStudents.forEach { student ->
                DropdownMenuItem(
                  text = { Text("${student.name} (#${student.rollNumber})") },
                  onClick = {
                    selectedStudent = student
                    showStudentDropdown = false
                    showError = ""
                    showSuccess = false
                  }
                )
              }
            }
          }

          // Marks Input
          OutlinedTextField(
            value = obtainedMarksStr,
            onValueChange = {
              obtainedMarksStr = it
              showError = ""
              showSuccess = false
            },
            label = { Text("Marks Obtained (Max: ${subject.maxMarks})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          if (showError.isNotEmpty()) {
            Text(showError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          if (showSuccess) {
            Text("Marks saved successfully!", color = StatusSuccessText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val student = selectedStudent
            if (student == null) {
              showError = "Please select a student."
              return@Button
            }
            val obtained = obtainedMarksStr.toIntOrNull()
            if (obtained == null || obtained < 0) {
              showError = "Enter valid non-negative marks."
              return@Button
            }
            if (obtained > subject.maxMarks) {
              showError = "Obtained marks cannot exceed Max Marks (${subject.maxMarks})."
              return@Button
            }
            repository.saveStudentMarks(
              studentId = student.id,
              term = exam.term,
              subjectName = subject.subjectName,
              obtainedMarks = obtained,
              maxMarks = subject.maxMarks
            )
            showSuccess = true
            obtainedMarksStr = ""
          }
        ) {
          Text("Save Marks")
        }
      },
      dismissButton = {
        TextButton(onClick = { activeSubjectForMarks = null }) {
          Text("Done")
        }
      }
    )
  }
}

