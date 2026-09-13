package com.example.ui.screens.students

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
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
  repository: ErpDataRepository,
  onNavigateToStudentDetail: (String) -> Unit,
  onShowAddStudent: () -> Unit
) {
  val windowSizeClass = rememberWindowSizeClass()
  val students by repository.students.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCohortFilter by remember { mutableStateOf("All Cohorts") }
  var selectedStudentForPane by remember { mutableStateOf<Student?>(students.firstOrNull()) }

  // Modal sheet state for mobile
  var showMobileDetailSheet by remember { mutableStateOf(false) }
  var activeMobileStudentId by remember { mutableStateOf<String?>(null) }

  val cohortFilters = listOf("All Cohorts", "Grade 10-A", "Grade 10-B", "Grade 11-Sci", "Grade 12-Arts")

  val filteredStudents = remember(students, searchQuery, selectedCohortFilter) {
    students.filter { st ->
      val matchesCohort = when (selectedCohortFilter) {
        "All Cohorts" -> true
        "Grade 10-A" -> st.classGrade == "10" && st.division == "A"
        "Grade 10-B" -> st.classGrade == "10" && st.division == "B"
        "Grade 11-Sci" -> st.classGrade == "11" || st.fullClass.contains("11")
        "Grade 12-Arts" -> st.classGrade == "12" || st.fullClass.contains("12")
        else -> true
      }
      val matchesQuery = if (searchQuery.isBlank()) true else {
        st.name.contains(searchQuery, ignoreCase = true) ||
        st.admissionNumber.contains(searchQuery, ignoreCase = true) ||
        st.parentName.contains(searchQuery, ignoreCase = true)
      }
      matchesCohort && matchesQuery
    }
  }

  val isSplitPane = windowSizeClass != WindowSizeClass.COMPACT_MOBILE

  Scaffold(
    containerColor = ScholaLinen,
    floatingActionButton = {
      if (currentUser.role == UserRole.PRINCIPAL) {
        ExtendedFloatingActionButton(
          onClick = onShowAddStudent,
          icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
          text = { Text("Enroll Scholar", fontWeight = FontWeight.Bold) },
          containerColor = ScholaTerracotta,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_admit_student")
        )
      }
    }
  ) { paddingValues ->
    if (isSplitPane) {
      // TABLET / DESKTOP SIDE-BY-SIDE TWO-PANE LAYOUT
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        // Left Master List Pane
        Column(
          modifier = Modifier
            .weight(1.1f)
            .fillMaxHeight()
            .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
        ) {
          ScholaInputField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Filter scholars by name, ID, or parent...",
            leadingIcon = Icons.Default.Search,
            onClear = { searchQuery = "" }
          )

          Spacer(modifier = Modifier.height(Spacing.s2))

          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
          ) {
            items(cohortFilters) { cohort ->
              val isSelected = selectedCohortFilter == cohort
              Surface(
                shape = RoundedCornerShape(Radius.pill),
                color = if (isSelected) ScholaTerracotta else ScholaSurface,
                modifier = Modifier
                  .clip(RoundedCornerShape(Radius.pill))
                  .border(1.dp, if (isSelected) ScholaTerracotta else ScholaBorder, RoundedCornerShape(Radius.pill))
                  .clickable { selectedCohortFilter = cohort }
              ) {
                Text(
                  text = cohort,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else ScholaTextPrimary,
                  modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(Spacing.s3))

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s2),
            contentPadding = PaddingValues(bottom = 90.dp)
          ) {
            items(filteredStudents, key = { it.id }) { student ->
              val isPaneSelected = selectedStudentForPane?.id == student.id
              ScholarCardItem(
                student = student,
                isSelected = isPaneSelected,
                onClick = { selectedStudentForPane = student }
              )
            }
          }
        }

        // 1px Vertical Divider
        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(ScholaBorder)
        )

        // Right Detail 360 Pane
        Box(
          modifier = Modifier
            .weight(1.3f)
            .fillMaxHeight()
            .padding(Spacing.s4)
        ) {
          if (selectedStudentForPane != null) {
            Student360DetailPane(
              student = selectedStudentForPane!!,
              repository = repository,
              onPayFee = { _, _ -> }
            )
          } else {
            EmptyStateView(
              title = "Select a Scholar",
              message = "Select a scholar from the directory list to inspect their Student 360 Profile."
            )
          }
        }
      }
    } else {
      // MOBILE SINGLE COLUMN LAYOUT WITH MODAL BOTTOM SHEET
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
      ) {
        ScholaInputField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = "Filter scholars by name, ID, parent...",
          leadingIcon = Icons.Default.Search,
          onClear = { searchQuery = "" }
        )

        Spacer(modifier = Modifier.height(Spacing.s2))

        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
          items(cohortFilters) { cohort ->
            val isSelected = selectedCohortFilter == cohort
            Surface(
              shape = RoundedCornerShape(Radius.pill),
              color = if (isSelected) ScholaTerracotta else ScholaSurface,
              modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .border(1.dp, if (isSelected) ScholaTerracotta else ScholaBorder, RoundedCornerShape(Radius.pill))
                .clickable { selectedCohortFilter = cohort }
            ) {
              Text(
                text = cohort,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else ScholaTextPrimary,
                modifier = Modifier.padding(horizontal = Spacing.s3, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(Spacing.s3))

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("students_list"),
          verticalArrangement = Arrangement.spacedBy(Spacing.s2),
          contentPadding = PaddingValues(bottom = 100.dp)
        ) {
          items(filteredStudents, key = { it.id }) { student ->
            ScholarCardItem(
              student = student,
              isSelected = false,
              onClick = {
                activeMobileStudentId = student.id
                showMobileDetailSheet = true
              }
            )
          }
        }
      }
    }
  }

  // Mobile Student 360 Modal Bottom Sheet
  if (showMobileDetailSheet && activeMobileStudentId != null) {
    val activeStudent = students.firstOrNull { it.id == activeMobileStudentId }
    if (activeStudent != null) {
      ModalBottomSheet(
        onDismissRequest = { showMobileDetailSheet = false },
        containerColor = ScholaLinen,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ) {
        Box(modifier = Modifier.padding(Spacing.s4)) {
          Student360DetailPane(
            student = activeStudent,
            repository = repository,
            onPayFee = { _, _ -> }
          )
        }
      }
    }
  }
}

@Composable
fun ScholarCardItem(
  student: Student,
  isSelected: Boolean = false,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(Radius.lg)
  Surface(
    shape = shape,
    color = if (isSelected) ScholaTerracottaContainer else ScholaSurface,
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .border(1.dp, if (isSelected) ScholaTerracotta else ScholaBorder, shape)
      .clickable(onClick = onClick)
      .testTag("student_row_${student.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(Spacing.cardPadding),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Avatar(name = student.name, size = 42.dp)
        Spacer(modifier = Modifier.width(Spacing.s3))
        Column {
          Text(
            text = student.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ScholaTextPrimary
          )
          Text(
            text = "Class ${student.fullClass} • Roll #${student.rollNumber} • ${student.admissionNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = ScholaMuted,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "GPA: ${student.gpa}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = ScholaTerracotta
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("•", color = ScholaMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${student.attendancePercent}% Att.",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (student.attendancePercent >= 90) StatusSuccessText else StatusWarningText
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        ScholaPillBadge(status = student.feeStatus.label)
        if (student.feePendingAmount > 0) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$${student.feePendingAmount / 100000L}k due",
            style = MaterialTheme.typography.labelSmall,
            color = StatusDangerText,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
