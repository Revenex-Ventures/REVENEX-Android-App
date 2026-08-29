package com.example.ui.screens.teachers

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
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun TeacherListScreen(
  repository: ErpDataRepository,
  onNavigateToTeacherDetail: (String) -> Unit,
  onShowAddTeacher: () -> Unit
) {
  val teachers by repository.teachers.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedDept by remember { mutableStateOf("All") }

  val departments = remember(teachers) {
    listOf("All") + teachers.filter { it.status == "ACTIVE" }.map { it.department }.distinct()
  }

  val filteredTeachers = remember(teachers, searchQuery, selectedDept) {
    teachers.filter { tch ->
      val matchesStatus = tch.status == "ACTIVE"
      val matchesDept = if (selectedDept == "All") true else tch.department == selectedDept
      val matchesSearch = if (searchQuery.isBlank()) true else {
        tch.name.contains(searchQuery, ignoreCase = true) ||
            tch.department.contains(searchQuery, ignoreCase = true) ||
            tch.subjects.any { it.contains(searchQuery, ignoreCase = true) }
      }
      matchesStatus && matchesDept && matchesSearch
    }
  }

  Scaffold(
    floatingActionButton = {
      if (currentUser.role == UserRole.PRINCIPAL) {
        ExtendedFloatingActionButton(
          onClick = onShowAddTeacher,
          icon = { Icon(Icons.Default.School, contentDescription = null) },
          text = { Text("Add Faculty", fontWeight = FontWeight.Bold) },
          containerColor = Color(0xFF6D28D9),
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_add_faculty")
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Search
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by faculty name, subject, or department...") },
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
          .testTag("teacher_search_input"),
        shape = RoundedCornerShape(14.dp),
        singleLine = true
      )

      // Department Filter Chips
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(departments) { dept ->
          FilterChip(
            selected = selectedDept == dept,
            onClick = { selectedDept = dept },
            label = { Text(if (dept == "All") "All Depts (${teachers.size})" else dept) },
            shape = RoundedCornerShape(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      if (filteredTeachers.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.SearchOff,
          title = "No Faculty Found",
          description = "Try adjusting your search query or department filter.",
          actionButtonText = "Reset Filters",
          onActionClick = {
            searchQuery = ""
            selectedDept = "All"
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
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredTeachers, key = { it.id }) { teacher ->
            TeacherCard(
              teacher = teacher,
              onClick = { onNavigateToTeacherDetail(teacher.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TeacherCard(
  teacher: Teacher,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("teacher_card_${teacher.employeeId}"),
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
        color = Color(0xFFEDE9FE),
        modifier = Modifier.size(46.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = teacher.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D28D9)
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
            text = teacher.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFEDE9FE)
          ) {
            Text(
              text = teacher.department,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF6D28D9),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = teacher.designation,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${teacher.weeklyPeriods} Periods/Wk",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Exp: ${teacher.experienceYears} Yrs",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = teacher.phone,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
