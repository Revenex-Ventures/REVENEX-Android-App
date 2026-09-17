package com.example.ui.screens.teachers

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.Teacher
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.Avatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ============================================================================
// FACULTY PROFILE / ACCOUNT — teacher-facing profile tab.
//   • Pastel identity card with photo upload (survives app restarts)
//   • Editable personal & professional details, persisted per teacher.id
// ============================================================================

@Composable
fun TeacherProfileScreen(
  repository: ErpDataRepository
) {
  val context = LocalContext.current
  val teachers by repository.teachers.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val teacher = remember(teachers, currentUser) {
    teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) }
      ?: teachers.firstOrNull { it.name.equals(currentUser.name, ignoreCase = true) }
      ?: teachers.firstOrNull()
  }

  if (teacher == null) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No faculty record linked to this account.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
      )
    }
    return
  }

  val saveScope = rememberCoroutineScope()
  val imagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    saveScope.launch {
      val savedPath = withContext(Dispatchers.IO) {
        try {
          val dir = File(context.filesDir, "avatars").apply { mkdirs() }
          val out = File(dir, "teacher_${teacher.id}.jpg")
          context.contentResolver.openInputStream(uri)?.use { input ->
            val bmp = BitmapFactory.decodeStream(input)
            out.outputStream().use { fos ->
              bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, fos)
            }
          }
          out.absolutePath
        } catch (e: Exception) {
          null
        }
      }
      if (savedPath != null) {
        repository.updateTeacher(teacher.copy(avatarUrl = savedPath))
        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    TeacherHeaderCard(
      teacher = teacher,
      onPickPhoto = { imagePicker.launch("image/*") }
    )

    EditableFacultyCard(
      teacher = teacher,
      onSave = { updated -> repository.updateTeacher(updated) }
    )

    FacultyRecordCard(teacher = teacher)

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun TeacherHeaderCard(
  teacher: Teacher,
  onPickPhoto: () -> Unit
) {
  val accent = ScholaSlateNavy
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.size(96.dp)) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, accent.copy(alpha = 0.25f), CircleShape)
            .clickable(onClick = onPickPhoto),
          contentAlignment = Alignment.Center
        ) {
          Avatar(
            name = teacher.name,
            size = 84.dp,
            imagePath = teacher.avatarUrl.ifBlank { null },
            background = accent,
            textColor = Color.White
          )
        }
        // Camera badge — drawn outside the clipped avatar so it is never cut off.
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(32.dp)
            .clip(CircleShape)
            .background(ScholaTerracottaDark)
            .border(2.dp, Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Filled.CameraAlt,
            contentDescription = "Change photo",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = teacher.name,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = ScholaTextPrimary
        )
        Text(
          text = teacher.designation,
          style = MaterialTheme.typography.bodySmall,
          color = ScholaTextSecondary
        )
        Text(
          text = "Employee ID ${teacher.employeeId}",
          style = MaterialTheme.typography.bodySmall,
          color = accent,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(status = teacher.status)
      }
    }
  }
}

@Composable
private fun EditableFacultyCard(
  teacher: Teacher,
  onSave: (Teacher) -> Unit
) {
  var editing by remember(teacher.id) { mutableStateOf(false) }
  var saved by remember(teacher.id) { mutableStateOf(true) }

  var name by remember(teacher.id) { mutableStateOf(teacher.name) }
  var dob by remember(teacher.id) { mutableStateOf(teacher.dob) }
  var phone by remember(teacher.id) { mutableStateOf(teacher.phone) }
  var email by remember(teacher.id) { mutableStateOf(teacher.email) }
  var department by remember(teacher.id) { mutableStateOf(teacher.department) }
  var designation by remember(teacher.id) { mutableStateOf(teacher.designation) }
  var qualification by remember(teacher.id) { mutableStateOf(teacher.qualification) }

  fun beginEdit() {
    name = teacher.name
    dob = teacher.dob
    phone = teacher.phone
    email = teacher.email
    department = teacher.department
    designation = teacher.designation
    qualification = teacher.qualification
    saved = true
    editing = true
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, ScholaBorder)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Personal & Professional Details",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = when {
              editing && saved -> "Saved"
              editing -> "Unsaved changes"
              else -> "View only"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (saved) StatusSuccessText else ScholaTerracotta,
            fontWeight = FontWeight.Bold
          )
          if (!editing) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
              onClick = { beginEdit() },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                Icons.Filled.Edit,
                contentDescription = "Edit details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (editing) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it; saved = false },
          label = { Text("Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = dob,
            onValueChange = { dob = it; saved = false },
            label = { Text("Date of Birth") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = department,
            onValueChange = { department = it; saved = false },
            label = { Text("Department") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it; saved = false },
          label = { Text("Phone") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it; saved = false },
          label = { Text("Email") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = designation,
          onValueChange = { designation = it; saved = false },
          label = { Text("Designation") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = qualification,
          onValueChange = { qualification = it; saved = false },
          label = { Text("Qualification") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              name = teacher.name
              dob = teacher.dob
              phone = teacher.phone
              email = teacher.email
              department = teacher.department
              designation = teacher.designation
              qualification = teacher.qualification
              saved = true
              editing = false
            },
            modifier = Modifier.weight(1f)
          ) {
            Text("Cancel")
          }
          Button(
            onClick = {
              onSave(
                teacher.copy(
                  name = name.trim(),
                  dob = dob.trim(),
                  phone = phone.trim(),
                  email = email.trim(),
                  department = department.trim(),
                  designation = designation.trim(),
                  qualification = qualification.trim()
                )
              )
              saved = true
              editing = false
            },
            enabled = !saved,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (saved) StatusSuccessContainer else ScholaTerracottaDark,
              contentColor = if (saved) StatusSuccessText else Color.White
            ),
            modifier = Modifier.weight(1f)
          ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
          }
        }
      } else {
        LabeledValue("Full Name", teacher.name)
        LabeledValue("Date of Birth", teacher.dob)
        LabeledValue("Phone", teacher.phone)
        LabeledValue("Email", teacher.email)
        LabeledValue("Designation", teacher.designation)
        LabeledValue("Department", teacher.department)
        LabeledValue("Qualification", teacher.qualification)
      }
    }
  }
}

@Composable
private fun FacultyRecordCard(teacher: Teacher) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = ScholaSurfaceWarm),
    border = BorderStroke(1.dp, ScholaBorder)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
      Text(
        text = "Faculty Record",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ScholaTextPrimary
      )
      Spacer(modifier = Modifier.height(12.dp))
      LabeledValue("Employee ID", teacher.employeeId)
      LabeledValue("Experience", "${teacher.experienceYears} years")
      LabeledValue("Weekly Periods", "${teacher.weeklyPeriods}")
      LabeledValue("Attendance", "${teacher.attendancePercent}%")
      LabeledValue("Joining Date", teacher.joiningDate)
      LabeledValue("Assigned Classes", teacher.assignedClasses.joinToString(", ").ifBlank { "—" })
      LabeledValue("Subjects", teacher.subjects.joinToString(", ").ifBlank { "—" })
    }
  }
}

@Composable
private fun LabeledValue(label: String, value: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = ScholaTextSecondary
    )
    Text(
      text = value.ifBlank { "—" },
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = ScholaTextPrimary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Start
    )
  }
}
