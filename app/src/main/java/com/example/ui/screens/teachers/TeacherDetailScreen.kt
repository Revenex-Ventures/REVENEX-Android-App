package com.example.ui.screens.teachers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDetailScreen(
  teacherId: String,
  repository: ErpDataRepository,
  onBack: () -> Unit
) {
  val teachers by repository.teachers.collectAsState()
  val timetable by repository.timetable.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val teacher = teachers.firstOrNull { it.id == teacherId } ?: teachers.first()
  val teacherClasses = timetable.filter { it.teacherName.contains(teacher.name.split(" ").lastOrNull() ?: "") }

  Scaffold(
    containerColor = GlassBgTransparent,
    topBar = {
      TopAppBar(
        title = { Text("Faculty Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ScholaTextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ScholaLinen)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
      verticalArrangement = Arrangement.spacedBy(Spacing.s3),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      // 1. HERO IDENTITY SURFACE (Dark Onyx, 22dp corners)
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.hero))
            .background(ScholaOnyx)
            .border(1.dp, ScholaOnyxBorder, RoundedCornerShape(Radius.hero))
            .padding(Spacing.cardPaddingLarge)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Avatar(name = teacher.name, size = 52.dp, background = ScholaSlateNavy)
            Spacer(modifier = Modifier.width(Spacing.s3))
            Column {
              Text(
                text = teacher.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ScholaOnyxText
              )
              Text(
                text = "${teacher.department} • ${teacher.designation}",
                style = MaterialTheme.typography.bodySmall,
                color = ScholaOnyxMuted
              )
              Text(
                text = "Emp ID: ${teacher.employeeId} • ${teacher.qualification}",
                style = MaterialTheme.typography.labelSmall,
                color = ScholaGold,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      // 2. CONTACT & CREDENTIALS
      item {
        AppCard(modifier = Modifier.fillMaxWidth()) {
          Text("Faculty Contact & Qualifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(Spacing.s2))
          InfoCard(title = "Email Address", value = teacher.email, icon = Icons.Default.Email)
          Spacer(modifier = Modifier.height(Spacing.s2))
          InfoCard(title = "Direct Phone", value = teacher.phone, icon = Icons.Default.Phone)
          Spacer(modifier = Modifier.height(Spacing.s2))
          InfoCard(title = "Experience", value = "${teacher.experienceYears} Years", icon = Icons.Default.WorkspacePremium)
          Spacer(modifier = Modifier.height(Spacing.s2))
          InfoCard(title = "Assigned Classes", value = teacher.assignedClasses.joinToString(", "), icon = Icons.Default.Class)
        }
      }

      // 3. WEEKLY TIMETABLE SLOTS
      item {
        SectionHeader(title = "Weekly Lecture Schedule (${teacherClasses.size})")
      }

      if (teacherClasses.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Lecture Slots",
            message = "No timetable slots currently assigned to this faculty member."
          )
        }
      } else {
        items(teacherClasses, key = { it.id }) { slot ->
          AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(slot.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${slot.dayOfWeek} • Period ${slot.periodNumber} (${slot.startTime} - ${slot.endTime})", style = MaterialTheme.typography.bodySmall, color = ScholaOnTerracottaContainer)
                Text("Class ${slot.classGrade}-${slot.division} • Room ${slot.roomNumber}", style = MaterialTheme.typography.labelSmall, color = ScholaMuted)
              }
              ScholaPillBadge(status = "Class ${slot.classGrade}-${slot.division}")
            }
          }
        }
      }
    }
  }
}
