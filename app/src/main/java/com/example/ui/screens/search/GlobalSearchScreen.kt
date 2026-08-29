package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

data class SearchResultItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: String,
  val icon: ImageVector,
  val iconTint: Color,
  val targetRoute: String
)

@Composable
fun GlobalSearchScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onBack: () -> Unit
) {
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()
  val notices by repository.notices.collectAsState()
  val assignments by repository.assignments.collectAsState()
  val books by repository.libraryBooks.collectAsState()
  val transport by repository.transportRoutes.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  var query by remember { mutableStateOf("") }

  val visibleStudents = remember(students, teachers, currentUser) {
    when (currentUser.role) {
      UserRole.PRINCIPAL -> students
      UserRole.TEACHER -> {
        val matchedTeacher = teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) }
        val allowedClasses = matchedTeacher?.assignedClasses ?: listOf(currentUser.associatedClass)
        students.filter { it.fullClass in allowedClasses }
      }
      UserRole.STUDENT, UserRole.PARENT -> {
        students.filter { it.name.equals(currentUser.name, ignoreCase = true) }
      }
    }
  }
  val canSeeFaculty = currentUser.role == UserRole.PRINCIPAL || currentUser.role == UserRole.TEACHER

  val results = remember(query, visibleStudents, teachers, notices, assignments, books, transport, canSeeFaculty) {
    if (query.isBlank()) emptyList()
    else {
      val q = query.trim()
      val list = mutableListOf<SearchResultItem>()

      // Students (role-scoped: teachers see only their classes; students/parents see only their own record)
      visibleStudents.filter { it.name.contains(q, ignoreCase = true) || it.admissionNumber.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.name,
            subtitle = "Student • Class ${it.fullClass} • Adm #${it.admissionNumber}",
            category = "STUDENT",
            icon = Icons.Default.Person,
            iconTint = RevenexBlue,
            targetRoute = Screen.StudentDetail.createRoute(it.id)
          )
        )
      }

      // Teachers (staff directory is restricted to staff; never surfaced to student/parent portals)
      if (canSeeFaculty) {
        teachers.filter { it.name.contains(q, ignoreCase = true) || it.department.contains(q, ignoreCase = true) || it.subjects.any { s -> s.contains(q, ignoreCase = true) } }.forEach {
          list.add(
            SearchResultItem(
              id = it.id,
              title = it.name,
              subtitle = "Faculty • ${it.department} (${it.designation})",
              category = "FACULTY",
              icon = Icons.Default.School,
              iconTint = Color(0xFF6D28D9),
              targetRoute = Screen.TeacherDetail.createRoute(it.id)
            )
          )
        }
      }

      // Notices
      notices.filter { it.title.contains(q, ignoreCase = true) || it.content.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.title,
            subtitle = "Circular • ${it.category.label} • ${it.publishedDate}",
            category = "CIRCULAR",
            icon = Icons.Default.Campaign,
            iconTint = Color(0xFFB45309),
            targetRoute = Screen.Notices.route
          )
        )
      }

      // Assignments
      assignments.filter { it.title.contains(q, ignoreCase = true) || it.subject.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.title,
            subtitle = "Homework • ${it.subject} • Due ${it.dueDate}",
            category = "HOMEWORK",
            icon = Icons.Default.Assignment,
            iconTint = Color(0xFF15803D),
            targetRoute = Screen.Homework.route
          )
        )
      }

      // Books
      books.filter { it.title.contains(q, ignoreCase = true) || it.author.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.title,
            subtitle = "Library Book • By ${it.author} • ${it.shelfLocation}",
            category = "LIBRARY",
            icon = Icons.Default.Book,
            iconTint = Color(0xFF0D9488),
            targetRoute = Screen.Library.route
          )
        )
      }

      // Transport
      transport.filter { it.routeName.contains(q, ignoreCase = true) || it.routeNumber.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = "${it.routeNumber}: ${it.routeName}",
            subtitle = "Bus Route • Driver: ${it.driverName} • ${it.vehicleNumber}",
            category = "TRANSPORT",
            icon = Icons.Default.DirectionsBus,
            iconTint = Color(0xFF0284C7),
            targetRoute = Screen.Transport.route
          )
        )
      }

      list
    }
  }

  Scaffold(
    topBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
          OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search students, faculty, notices, fees...") },
            modifier = Modifier
              .weight(1f)
              .testTag("global_search_input_field"),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            trailingIcon = {
              if (query.isNotBlank()) {
                IconButton(onClick = { query = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
              }
            }
          )
        }
      }
    }
  ) { paddingValues ->
    if (query.isBlank()) {
      EmptyStateView(
        icon = Icons.Default.Search,
        title = "Omni Search Across School",
        description = "Type student names, admission IDs, teacher designations, notice titles, library books or bus routes.",
        modifier = Modifier.padding(paddingValues)
      )
    } else if (results.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.SearchOff,
        title = "No Matches Found",
        description = "No ERP records matched \"$query\". Try a different search term.",
        modifier = Modifier.padding(paddingValues)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .testTag("global_search_results_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(results, key = { "${it.category}_${it.id}" }) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateTo(item.targetRoute) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(item.iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = null,
                  tint = item.iconTint,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                  ) {
                    Text(
                      text = item.category,
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = item.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}
