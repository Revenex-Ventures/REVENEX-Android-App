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
import com.example.ui.components.*
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
  val cloudDemo = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO
  val context = LocalContext.current
  val teachers by repository.teachers.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val teacher = remember(teachers, currentUser) {
    teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) }
      ?: teachers.firstOrNull { it.name.equals(currentUser.name, ignoreCase = true) }
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
      .imePadding().verticalScroll(rememberScrollState())
      .padding(Spacing.screenPadding),
    verticalArrangement = Arrangement.spacedBy(Spacing.xl24)
  ) {
    TeacherHeaderCard(
      teacher = teacher,
      onPickPhoto = { if (cloudDemo) Toast.makeText(context, "Faculty updates are managed by the school office in this demo.", Toast.LENGTH_LONG).show() else imagePicker.launch("image/*") }
    )

    if (cloudDemo) {
      Text("Faculty updates are managed by the school office in this demo.", style = MaterialTheme.typography.bodyMedium)
    } else EditableFacultyCard(
      teacher = teacher,
      onSave = { updated -> repository.updateTeacher(updated) }
    )

    FacultyRecordCard(teacher = teacher)

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun TeacherHeaderCard(teacher: Teacher,onPickPhoto: () -> Unit) {
  SchoolGroup {
    Row(Modifier.fillMaxWidth().padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      Box(Modifier.size(48.dp).clickable(onClick=onPickPhoto),contentAlignment=Alignment.Center) {
        Avatar(teacher.name,size=40.dp,imagePath=teacher.avatarUrl.ifBlank {null},background=SemanticColors.lavenderContainer,textColor=SemanticColors.onLavender)
      }
      Column(Modifier.weight(1f)) {
        Text(teacher.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        Text(teacher.designation,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text(teacher.employeeId,style=MaterialTheme.typography.labelSmall)
      }
      IconButton(onClick=onPickPhoto){Icon(Icons.Filled.CameraAlt,"Change photo")}
    }
  }
}

@Composable
private fun EditableFacultyCard(teacher: Teacher,onSave: (Teacher) -> Unit) {
  var editing by remember(teacher.id){mutableStateOf(false)}
  var saved by remember(teacher.id){mutableStateOf(true)}
  var name by remember(teacher.id){mutableStateOf(teacher.name)}
  var dob by remember(teacher.id){mutableStateOf(teacher.dob)}
  var phone by remember(teacher.id){mutableStateOf(teacher.phone)}
  var email by remember(teacher.id){mutableStateOf(teacher.email)}
  var department by remember(teacher.id){mutableStateOf(teacher.department)}
  var designation by remember(teacher.id){mutableStateOf(teacher.designation)}
  var qualification by remember(teacher.id){mutableStateOf(teacher.qualification)}
  fun reset(){name=teacher.name;dob=teacher.dob;phone=teacher.phone;email=teacher.email;department=teacher.department;designation=teacher.designation;qualification=teacher.qualification;saved=true}
  Column(verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    SchoolSection("Personal details",if(editing)"Editing" else "Edit",{if(!editing){reset();editing=true}}) {
      if(editing) {
        OutlinedTextField(name,{name=it;saved=false},Modifier.fillMaxWidth(),label={Text("Full name")},singleLine=true)
        OutlinedTextField(dob,{dob=it;saved=false},Modifier.fillMaxWidth(),label={Text("Date of birth")},singleLine=true)
      } else SchoolGroup {Column(Modifier.padding(Spacing.md12)){LabeledValue("Full name",teacher.name);LabeledValue("Date of birth",teacher.dob)}}
    }
    SchoolSection("Contact") {
      if(editing) {
        OutlinedTextField(phone,{phone=it;saved=false},Modifier.fillMaxWidth(),label={Text("Phone")},singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Phone))
        OutlinedTextField(email,{email=it;saved=false},Modifier.fillMaxWidth(),label={Text("Email")},singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Email))
      } else SchoolGroup {Column(Modifier.padding(Spacing.md12)){LabeledValue("Phone",teacher.phone);LabeledValue("Email",teacher.email)}}
    }
    SchoolSection("Professional information") {
      if(editing) {
        OutlinedTextField(department,{department=it;saved=false},Modifier.fillMaxWidth(),label={Text("Department")},singleLine=true)
        OutlinedTextField(designation,{designation=it;saved=false},Modifier.fillMaxWidth(),label={Text("Designation")},singleLine=true)
        OutlinedTextField(qualification,{qualification=it;saved=false},Modifier.fillMaxWidth(),label={Text("Qualification")},minLines=2)
      } else SchoolGroup {Column(Modifier.padding(Spacing.md12)){LabeledValue("Department",teacher.department);LabeledValue("Designation",teacher.designation);LabeledValue("Qualification",teacher.qualification)}}
    }
    if(editing) {
      Text(if(saved)"No unsaved changes" else "Unsaved changes",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        OutlinedButton(onClick={reset();editing=false},modifier=Modifier.weight(1f)){Text("Cancel")}
        Button(onClick={onSave(teacher.copy(name=name.trim(),dob=dob.trim(),phone=phone.trim(),email=email.trim(),department=department.trim(),designation=designation.trim(),qualification=qualification.trim()));saved=true;editing=false},enabled=!saved,modifier=Modifier.weight(1f)){Text("Save changes")}
      }
    }
  }
}

@Composable
private fun FacultyRecordCard(teacher: Teacher) {
  SchoolSection("Faculty record") {SchoolGroup {Column(Modifier.padding(Spacing.md12)) {
    LabeledValue("Employee ID",teacher.employeeId)
    LabeledValue("Experience / joined","${teacher.experienceYears} years · ${teacher.joiningDate}")
    LabeledValue("Weekly periods",teacher.weeklyPeriods.toString())
    LabeledValue("Recorded attendance","${teacher.attendancePercent}%")
    LabeledValue("Assigned classes",teacher.assignedClasses.joinToString(", "))
    LabeledValue("Subjects",teacher.subjects.joinToString(", "))
    LabeledValue("Status",teacher.status)
  }}}
}

@Composable
private fun LabeledValue(label: String,value: String) {
  Column(Modifier.fillMaxWidth().padding(vertical=Spacing.xs4)) {
    Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value.ifBlank {"Not provided"},style=MaterialTheme.typography.bodyMedium)
  }
}
