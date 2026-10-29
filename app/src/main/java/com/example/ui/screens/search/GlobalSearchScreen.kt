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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
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
  val invoices by repository.invoices.collectAsState()

  var query by remember { mutableStateOf("") }

  val results = remember(query, students, teachers, notices, assignments, invoices) {
    if (query.isBlank()) emptyList()
    else {
      val q = query.trim()
      val list = mutableListOf<SearchResultItem>()

      // Students
      students.filter { it.name.contains(q, ignoreCase = true) || it.admissionNumber.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.name,
            subtitle = "Student · Class ${it.fullClass} · Admission ${it.admissionNumber}",
            category = "SCHOLAR",
            icon = Icons.Default.Person,
            iconTint = ScholaTerracotta,
            targetRoute = Screen.StudentDetail.createRoute(it.id)
          )
        )
      }

      // Teachers
      teachers.filter { it.name.contains(q, ignoreCase = true) || it.department.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.name,
            subtitle = "Faculty • ${it.department} (${it.designation})",
            category = "FACULTY",
            icon = Icons.Default.School,
            iconTint = ScholaSlateNavy,
            targetRoute = Screen.TeacherDetail.createRoute(it.id)
          )
        )
      }

      // Invoices
      invoices.filter { it.invoiceNumber.contains(q, ignoreCase = true) || it.studentName.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = "${it.invoiceNumber}: ${it.studentName}",
            subtitle = "Ledger • ${it.title} • ₹${it.paidAmount / 100L} paid / ₹${it.totalAmount / 100L}",
            category = "INVOICE",
            icon = Icons.Default.Receipt,
            iconTint = ScholaGold,
            targetRoute = Screen.Fees.route
          )
        )
      }

      assignments.filter { it.title.contains(q, true) || it.subject.contains(q, true) }.forEach {
        list.add(SearchResultItem(it.id, it.title, "${it.subject} · Due ${it.dueDate}",
          "ASSIGNMENT", Icons.Default.Assignment, ScholaTerracotta, Screen.Homework.route))
      }

      // Notices
      notices.filter { it.title.contains(q, ignoreCase = true) || it.content.contains(q, ignoreCase = true) }.forEach {
        list.add(
          SearchResultItem(
            id = it.id,
            title = it.title,
            subtitle = "Circular • ${it.category.label} • ${it.publishedDate}",
            category = "BROADCAST",
            icon = Icons.Default.Campaign,
            iconTint = ScholaSlateNavy,
            targetRoute = Screen.Notices.route
          )
        )
      }

      list
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassBgTransparent,
        tonalElevation = Elev.e0
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
          }
          ScholaInputField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search students, faculty, invoices or notices",
            leadingIcon = Icons.Default.Search,
            onClear = { query = "" },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  ) { paddingValues ->
    if (query.isBlank()) {
      EmptyStateView(
        icon = Icons.Default.Search,
        title = "Search Revenex",
        description = "Search the school records available to your account.",
        modifier = Modifier.padding(paddingValues)
      )
    } else if (results.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.SearchOff,
        title = "No Matches Found",
        description = "No educational records matched \"$query\".",
        modifier = Modifier.padding(paddingValues)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
          .testTag("global_search_results_list"),
        verticalArrangement = Arrangement.spacedBy(Spacing.s2),
        contentPadding = PaddingValues(bottom = Spacing.xl24)
      ) {
        results.groupBy {it.category}.forEach {(category,entries)->
          item {Text(when(category){"SCHOLAR"->"Students";"FACULTY"->"Faculty";"INVOICE"->"Invoices";"ASSIGNMENT"->"Assignments";else->"Notices"},Modifier.padding(top=Spacing.md12),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)}
          item {SchoolGroup {entries.forEachIndexed {index,result->
            SchoolRow(result.title,result.subtitle,result.icon,{onNavigateTo(result.targetRoute)})
            if(index<entries.lastIndex) HorizontalDivider()
          }}}
        }

      }
    }
  }
}
