package com.example.ui.screens.students

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
import com.example.data.model.FeeStatus
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
  repository: ErpDataRepository,
  onNavigateToStudentDetail: (String) -> Unit,
  onShowAddStudent: () -> Unit
) {
  val students by repository.students.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val teachers by repository.teachers.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedClassFilter by remember { mutableStateOf("All") }

  val allowedClasses = remember(teachers, currentUser) {
    if (currentUser.role == UserRole.TEACHER) {
      val matchedTeacher = teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) }
      matchedTeacher?.assignedClasses ?: listOf(currentUser.associatedClass)
    } else {
      emptyList()
    }
  }

  val filteredStudents = remember(students, searchQuery, selectedClassFilter, currentUser, allowedClasses) {
    students.filter { st ->
      val matchesStatus = st.status == "ACTIVE"
      
      val isAllowed = if (currentUser.role == UserRole.TEACHER) {
        st.fullClass in allowedClasses
      } else {
        true
      }

      val matchesClass = if (selectedClassFilter == "All") true else st.classGrade == selectedClassFilter
      val matchesSearch = if (searchQuery.isBlank()) true else {
        st.name.contains(searchQuery, ignoreCase = true) ||
            st.admissionNumber.contains(searchQuery, ignoreCase = true) ||
            st.parentName.contains(searchQuery, ignoreCase = true)
      }
      matchesStatus && isAllowed && matchesClass && matchesSearch
    }
  }

  Scaffold(
    floatingActionButton = {
      if (currentUser.role == UserRole.PRINCIPAL) {
        ExtendedFloatingActionButton(
          onClick = onShowAddStudent,
          icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Admit Student") },
          text = { Text("Admit Student", fontWeight = FontWeight.Bold) },
          containerColor = RevenexBlue,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_admit_student")
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by student name, adm no, or parent...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("student_search_input"),
        shape = RoundedCornerShape(14.dp),
        singleLine = true
      )

      // Grade Filter Chips
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val classFilters = listOf("All") + students.map { it.classGrade }.distinct().sortedByDescending { it.toIntOrNull() ?: 0 }
        items(classFilters) { filter ->
          FilterChip(
            selected = selectedClassFilter == filter,
            onClick = { selectedClassFilter = filter },
            label = { Text(if (filter == "All") "All Classes (${students.size})" else "Class $filter") },
            shape = RoundedCornerShape(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Student Roster
      if (filteredStudents.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.SearchOff,
          title = "No Students Found",
          description = "No matching student records found for the selected filter.",
          actionButtonText = "Clear Filters",
          onActionClick = {
            searchQuery = ""
            selectedClassFilter = "All"
          }
        )
      } else {
        val listState = rememberLazyListState()
        LaunchedEffect(key1 = Unit) {
          listState.scrollToItem(0)
        }
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredStudents, key = { it.id }) { student ->
            StudentRosterCard(
              student = student,
              onClick = { onNavigateToStudentDetail(student.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StudentRosterCard(
  student: Student,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("student_card_${student.admissionNumber}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = Color(student.avatarColorHex),
        modifier = Modifier.size(46.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = student.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          StatusBadge(status = student.feeStatus.label, type = "fee")
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Class ${student.fullClass} • Roll #${student.rollNumber} • Adm #${student.admissionNumber}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Att: ${student.attendancePercent}%",
            style = MaterialTheme.typography.labelSmall,
            color = if (student.attendancePercent >= 90.0) StatusSuccessText else StatusWarningText,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "GPA: ${student.gpa}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Parent: ${student.parentName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
