package com.example.ui.screens.teachers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ErpDataRepository
import com.example.data.model.UserRole
import com.example.data.model.Teacher
import com.example.ui.screens.students.InfoCard
import com.example.ui.theme.*

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

  var showEditDialog by remember { mutableStateOf(false) }
  var showDeactivateConfirm by remember { mutableStateOf(false) }

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
          Text(
            text = "Faculty Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )

          if (currentUser.role == UserRole.PRINCIPAL) {
            IconButton(
              onClick = { showEditDialog = true },
              modifier = Modifier.testTag("btn_edit_teacher")
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Faculty")
            }
            IconButton(
              onClick = { showDeactivateConfirm = true },
              modifier = Modifier.testTag("btn_deactivate_teacher")
            ) {
              Icon(Icons.Default.PersonRemove, contentDescription = "Deactivate Faculty", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  ) { paddingValues ->
    if (showDeactivateConfirm) {
      AlertDialog(
        onDismissRequest = { showDeactivateConfirm = false },
        title = { Text("Deactivate Faculty Profile?", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to deactivate ${teacher.name}'s profile? They will no longer appear in active staff rosters or directories.") },
        confirmButton = {
          Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = {
              repository.updateTeacher(teacher.copy(status = "INACTIVE"))
              showDeactivateConfirm = false
              onBack()
            }
          ) {
            Text("Deactivate")
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeactivateConfirm = false }) {
            Text("Cancel")
          }
        }
      )
    }

    if (showEditDialog) {
      var editName by remember { mutableStateOf(teacher.name) }
      var editDept by remember { mutableStateOf(teacher.department) }
      var editDesig by remember { mutableStateOf(teacher.designation) }
      var editQual by remember { mutableStateOf(teacher.qualification) }
      var editEmail by remember { mutableStateOf(teacher.email) }
      var editPhone by remember { mutableStateOf(teacher.phone) }
      var editWeeklyPeriods by remember { mutableStateOf(teacher.weeklyPeriods.toString()) }

      AlertDialog(
        onDismissRequest = { showEditDialog = false },
        title = { Text("Edit Faculty Details", fontWeight = FontWeight.Bold) },
        text = {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Faculty Full Name") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editDept,
                onValueChange = { editDept = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editDesig,
                onValueChange = { editDesig = it },
                label = { Text("Designation") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editQual,
                onValueChange = { editQual = it },
                label = { Text("Qualification") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                label = { Text("Contact Phone") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editWeeklyPeriods,
                onValueChange = { editWeeklyPeriods = it },
                label = { Text("Weekly Periods Load") },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val periods = editWeeklyPeriods.toIntOrNull() ?: teacher.weeklyPeriods
              repository.updateTeacher(
                teacher.copy(
                  name = editName.trim(),
                  department = editDept.trim(),
                  designation = editDesig.trim(),
                  qualification = editQual.trim(),
                  email = editEmail.trim(),
                  phone = editPhone.trim(),
                  weeklyPeriods = periods
                )
              )
              showEditDialog = false
            }
          ) {
            Text("Save Changes")
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("teacher_detail_view"),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(60.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = teacher.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.width(16.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = teacher.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = teacher.designation,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                  text = "Employee ID: ${teacher.employeeId}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.7f)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("DEPARTMENT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(teacher.department, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("EXPERIENCE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${teacher.experienceYears} Years", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("WEEKLY LOAD", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${teacher.weeklyPeriods} Periods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }

      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          InfoCard(title = "Academic Qualifications", value = teacher.qualification, subtitle = "Verified University Degree", icon = Icons.Default.School)
          InfoCard(title = "Email Address", value = teacher.email, subtitle = "Official institutional inbox", icon = Icons.Default.Email)
          InfoCard(title = "Mobile Number", value = teacher.phone, subtitle = "Emergency faculty hotline", icon = Icons.Default.Phone)
          InfoCard(title = "Subjects Handled", value = teacher.subjects.joinToString(", "), subtitle = "Senior Secondary Curriculum", icon = Icons.Default.MenuBook)
          InfoCard(title = "Assigned Grades", value = teacher.assignedClasses.joinToString(", "), subtitle = "Active teaching classes", icon = Icons.Default.Groups)
          InfoCard(title = "Joining Date", value = teacher.joiningDate, subtitle = "Permanent School Faculty", icon = Icons.Default.DateRange)
        }
      }
    }
  }
}
