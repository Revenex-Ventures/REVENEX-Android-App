package com.example.ui.screens.teachers

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
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    containerColor = GlassBgTransparent,
    floatingActionButton = {
      if (currentUser.role == UserRole.PRINCIPAL) {
        ExtendedFloatingActionButton(
          onClick = onShowAddTeacher,
          icon = { Icon(Icons.Default.School, contentDescription = null) },
          text = { Text("Add Faculty", fontWeight = FontWeight.Bold) },
          containerColor = ScholaSlateNavy,
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
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
    ) {
      // Search Bar
      ScholaInputField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = "Search faculty by name, department, or subject...",
        leadingIcon = Icons.Default.Search,
        onClear = { searchQuery = "" }
      )

      Spacer(modifier = Modifier.height(Spacing.s2))

      // Department Filter Chips
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
      ) {
        items(departments) { dept ->
          val isSelected = selectedDept == dept
          Surface(
            shape = RoundedCornerShape(Radius.pill),
            color = if (isSelected) ScholaSlateNavy else ScholaSurface,
            modifier = Modifier
              .clip(RoundedCornerShape(Radius.pill))
              .border(1.dp, if (isSelected) ScholaSlateNavy else ScholaBorder, RoundedCornerShape(Radius.pill))
              .clickable { selectedDept = dept }
          ) {
            Text(
              text = dept,
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
        contentPadding = PaddingValues(bottom = 100.dp)
      ) {
        items(filteredTeachers, key = { it.id }) { teacher ->
          AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigateToTeacherDetail(teacher.id) }
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Avatar(name = teacher.name, size = 42.dp, background = ScholaSlateNavy)
                Spacer(modifier = Modifier.width(Spacing.s3))
                Column {
                  Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScholaTextPrimary
                  )
                  Text(
                    text = "${teacher.department} • ${teacher.designation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScholaMuted,
                    fontSize = 11.sp
                  )
                  Text(
                    text = "Subjects: ${teacher.subjects.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ScholaOnTerracottaContainer,
                    fontSize = 10.sp
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                ScholaPillBadge(status = "${teacher.weeklyPeriods} Periods/Wk")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${teacher.experienceYears}y Exp.",
                  style = MaterialTheme.typography.labelSmall,
                  color = ScholaMuted,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
