package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.data.datasource.FirebaseStorageManager
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AddStudentDialog(
  existingStudentIds: List<String> = emptyList(),
  existingAdmissionNumbers: List<String> = emptyList(),
  onDismiss: () -> Unit,
  onAddStudent: (Student) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var studentId by remember { mutableStateOf("stu_${(1007..9999).random()}") }
  var admissionNo by remember { mutableStateOf("REV-2026-${(1000..9999).random()}") }
  var rollNo by remember { mutableStateOf("") }
  var classGrade by remember { mutableStateOf("") }
  var division by remember { mutableStateOf("A") }
  var dob by remember { mutableStateOf("") }
  var gender by remember { mutableStateOf("Male") }
  var bloodGroup by remember { mutableStateOf("") }
  var parentName by remember { mutableStateOf("") }
  var parentPhone by remember { mutableStateOf("+91 ") }
  var parentEmail by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "New Student Admission",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Fill all required fields (*) to complete admission",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it; validationError = null },
          label = { Text("Full Student Name *") },
          placeholder = { Text("e.g., Aarav Patel") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_student_name"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it; validationError = null },
            label = { Text("Student ID *") },
            placeholder = { Text("stu_1011") },
            modifier = Modifier.weight(1.2f),
            singleLine = true
          )
          OutlinedTextField(
            value = admissionNo,
            onValueChange = { admissionNo = it; validationError = null },
            label = { Text("Admission No *") },
            placeholder = { Text("REV-2026-1001") },
            modifier = Modifier.weight(1.8f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = classGrade,
            onValueChange = { classGrade = it; validationError = null },
            label = { Text("Class * (1-12)") },
            placeholder = { Text("10") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = division,
            onValueChange = { division = it },
            label = { Text("Division") },
            placeholder = { Text("A") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = rollNo,
            onValueChange = { rollNo = it },
            label = { Text("Roll No") },
            placeholder = { Text("16") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = dob,
          onValueChange = { dob = it; validationError = null },
          label = { Text("Date of Birth * (DD/MM/YYYY)") },
          placeholder = { Text("15/08/2009") },
          leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = bloodGroup,
            onValueChange = { bloodGroup = it },
            label = { Text("Blood Group") },
            placeholder = { Text("O+") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section divider
        Text(
          text = "Parent / Guardian Details",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = parentName,
          onValueChange = { parentName = it; validationError = null },
          label = { Text("Parent / Guardian Full Name *") },
          leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = parentPhone,
          onValueChange = { parentPhone = it; validationError = null },
          label = { Text("Parent Phone Number * (+91 XXXXX XXXXX)") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = parentEmail,
          onValueChange = { parentEmail = it },
          label = { Text("Parent Email (Optional)") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Residential Address") },
          leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )

        // Validation error display
        if (validationError != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = validationError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            // Validation
            val classNum = classGrade.trim().toIntOrNull()
            when {
              name.trim().length < 3 -> {
                validationError = "Student name must be at least 3 characters."
                return@Button
              }
              studentId.trim().isBlank() -> {
                validationError = "Student ID is required."
                return@Button
              }
              existingStudentIds.any { it.equals(studentId.trim(), ignoreCase = true) } -> {
                validationError = "Student ID already exists in the system."
                return@Button
              }
              admissionNo.trim().isBlank() -> {
                validationError = "Admission number is required."
                return@Button
              }
              existingAdmissionNumbers.any { it.equals(admissionNo.trim(), ignoreCase = true) } -> {
                validationError = "Admission number already exists."
                return@Button
              }
              classGrade.isBlank() || classNum == null || classNum !in 1..12 -> {
                validationError = "Valid Class/Grade (1-12) is required."
                return@Button
              }
              dob.isBlank() || !Regex("""^\d{2}/\d{2}/\d{4}$""").matches(dob.trim()) -> {
                validationError = "Date of Birth must be in DD/MM/YYYY format."
                return@Button
              }
              parentName.trim().length < 3 -> {
                validationError = "Parent/Guardian name must be at least 3 characters."
                return@Button
              }
              parentPhone.replace("+91", "").trim().replace(" ", "").length < 10 -> {
                validationError = "Please enter a valid 10-digit Indian phone number."
                return@Button
              }
              else -> {
                val indiaDateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("en", "IN"))
                indiaDateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")

                val newStudent = Student(
                  id = studentId.trim(),
                  admissionNumber = admissionNo.trim(),
                  rollNumber = rollNo.toIntOrNull() ?: 1,
                  name = name.trim(),
                  classGrade = classGrade.trim(),
                  division = division.trim().uppercase().ifBlank { "A" },
                  dob = dob.trim(),
                  gender = gender.ifBlank { "Not Specified" },
                  bloodGroup = bloodGroup.ifBlank { "Unknown" },
                  parentName = parentName.trim(),
                  parentPhone = parentPhone.trim(),
                  parentEmail = parentEmail.ifBlank { "${name.lowercase().replace(" ", ".")}@parent.in" },
                  address = address.ifBlank { "Address pending" },
                  admissionDate = indiaDateFormat.format(java.util.Date()),
                  attendancePercent = 100.0,
                  feeStatus = FeeStatus.PENDING,
                  feePendingAmount = 4500000L,
                  rank = 1,
                  gpa = 0.0,
                  status = "ACTIVE"
                )
                onAddStudent(newStudent)
                onDismiss()
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_add_student_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue),
          enabled = name.isNotBlank() && parentName.isNotBlank() && classGrade.isNotBlank()
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Complete Admission & Save", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}


@Composable
fun AddTeacherDialog(
  existingEmployeeIds: List<String> = emptyList(),
  onDismiss: () -> Unit,
  onAddTeacher: (Teacher) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var employeeId by remember { mutableStateOf("EMP-${(200..999).random()}") }
  var dob by remember { mutableStateOf("") }
  var gender by remember { mutableStateOf("Male") }
  var department by remember { mutableStateOf("Mathematics") }
  var designation by remember { mutableStateOf("Senior Faculty") }
  var qualification by remember { mutableStateOf("M.Sc., B.Ed") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+91 ") }
  var assignedClass by remember { mutableStateOf("10-A, 9-B") }
  var subject by remember { mutableStateOf("Mathematics") }
  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Add Faculty Member",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Fill faculty information to register staff profile",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it; validationError = null },
          label = { Text("Faculty Full Name *") },
          placeholder = { Text("e.g. Prof. Sunita Rao") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_teacher_name"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = employeeId,
            onValueChange = { employeeId = it; validationError = null },
            label = { Text("Employee ID *") },
            placeholder = { Text("EMP-201") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department") },
            placeholder = { Text("Science") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = dob,
          onValueChange = { dob = it; validationError = null },
          label = { Text("Date of Birth * (DD/MM/YYYY)") },
          placeholder = { Text("12/04/1982") },
          leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text("Designation") },
            placeholder = { Text("Head of Dept") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = qualification,
            onValueChange = { qualification = it },
            label = { Text("Qualification") },
            placeholder = { Text("Ph.D, M.Ed") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Primary Subject(s)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it; validationError = null },
          label = { Text("Institutional Email *") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          placeholder = { Text("teacher@revenexschool.edu.in") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it; validationError = null },
          label = { Text("Contact Phone Number * (+91 XXXXX XXXXX)") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = assignedClass,
          onValueChange = { assignedClass = it },
          label = { Text("Assigned Classes (comma separated)") },
          leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
          placeholder = { Text("10-A, 9-B") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Validation error display
        if (validationError != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = validationError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            when {
              name.trim().length < 3 -> {
                validationError = "Faculty name must be at least 3 characters."
                return@Button
              }
              employeeId.trim().isBlank() -> {
                validationError = "Employee ID is required."
                return@Button
              }
              existingEmployeeIds.any { it.equals(employeeId.trim(), ignoreCase = true) } -> {
                validationError = "Employee ID already exists."
                return@Button
              }
              dob.isBlank() || !Regex("""^\d{2}/\d{2}/\d{4}$""").matches(dob.trim()) -> {
                validationError = "Date of Birth must be in DD/MM/YYYY format."
                return@Button
              }
              phone.replace("+91", "").trim().replace(" ", "").length < 10 -> {
                validationError = "Please enter a valid 10-digit Indian phone number."
                return@Button
              }
              else -> {
                val indiaDateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("en", "IN"))
                indiaDateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")

                val newTeacher = Teacher(
                  id = "tch_${UUID.randomUUID().toString().take(8)}",
                  employeeId = employeeId.trim().uppercase(),
                  name = name.trim(),
                  dob = dob.trim(),
                  gender = gender.trim(),
                  department = department.trim().ifBlank { "General" },
                  designation = designation.trim().ifBlank { "Faculty Member" },
                  qualification = qualification.trim().ifBlank { "B.Ed" },
                  email = email.ifBlank { "${name.lowercase().replace(" ", ".")}@revenexschool.edu.in" },
                  phone = phone.trim(),
                  assignedClasses = assignedClass.split(",").map { it.trim() }.filter { it.isNotBlank() },
                  subjects = listOf(subject.trim().ifBlank { "General Studies" }),
                  weeklyPeriods = 22,
                  attendancePercent = 100.0,
                  experienceYears = 5,
                  joiningDate = indiaDateFormat.format(java.util.Date()),
                  status = "ACTIVE"
                )
                onAddTeacher(newTeacher)
                onDismiss()
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_add_teacher_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue),
          enabled = name.isNotBlank() && employeeId.isNotBlank()
        ) {
          Icon(Icons.Default.School, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save Faculty Profile", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CreateNoticeDialog(
  onDismiss: () -> Unit,
  onPublish: (SchoolNotice) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var category by remember { mutableStateOf(NoticeCategory.CIRCULAR) }
  var targetAudience by remember { mutableStateOf("All School") }
  var isImportant by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Publish Notice / Circular",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Notice Title *") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_notice_title"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Detailed Notice Content *") },
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .testTag("input_notice_content"),
          maxLines = 6
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(NoticeCategory.CIRCULAR, NoticeCategory.EVENT, NoticeCategory.EXAM, NoticeCategory.HOLIDAY).forEach { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat.label, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Mark as High Priority Alert",
            style = MaterialTheme.typography.bodyMedium
          )
          Switch(
            checked = isImportant,
            onCheckedChange = { isImportant = it }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            if (title.isNotBlank() && content.isNotBlank()) {
              val notice = SchoolNotice(
                id = "not_${System.currentTimeMillis()}",
                title = title.trim(),
                content = content.trim(),
                category = category,
                publishedDate = "Today",
                authorName = "Principal / Admin Office",
                authorRole = "Administration",
                targetAudience = targetAudience,
                isImportant = isImportant
              )
              onPublish(notice)
              onDismiss()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("submit_publish_notice_button"),
          enabled = title.isNotBlank() && content.isNotBlank()
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Publish School Announcement")
        }
      }
    }
  }
}

@Composable
fun CreateAssignmentDialog(
  teacherName: String,
  repository: ErpDataRepository,
  onDismiss: () -> Unit,
  onCreate: (HomeworkAssignment) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("Mathematics") }
  var classGrade by remember { mutableStateOf("10") }
  var division by remember { mutableStateOf("A") }
  var dueDate by remember { mutableStateOf("05 Sep 2026") }
  var instructions by remember { mutableStateOf("") }
  var maxPoints by remember { mutableStateOf("20") }
  val context = LocalContext.current

  val todayDate = remember {
    SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
  }

  var selectedFileName by remember { mutableStateOf("") }
  var selectedFileMime by remember { mutableStateOf("") }
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var attachmentError by remember { mutableStateOf<String?>(null) }
  var isUploading by remember { mutableStateOf(false) }

  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    attachmentError = null
    if (uri != null) {
      val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
      if (!FirebaseStorageManager.isAllowedDocumentType(mime)) {
        attachmentError = "Only PDF, DOC or DOCX files are allowed as attachments."
        selectedFileName = ""
        selectedFileMime = ""
        selectedFileUri = null
      } else {
        val nameFromUri = uri.lastPathSegment?.substringAfterLast("/") ?: "attachment"
        val fallbackName = when (mime.lowercase()) {
          "application/pdf" -> "document.pdf"
          "application/msword" -> "document.doc"
          else -> "document.docx"
        }
        selectedFileName = if (nameFromUri.contains(".")) nameFromUri else fallbackName
        selectedFileMime = mime
        selectedFileUri = uri
      }
    } else {
      selectedFileName = ""
      selectedFileMime = ""
      selectedFileUri = null
    }
  }

  val createScope = rememberCoroutineScope()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Homework Assignment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Assignment Title *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.weight(1.5f),
            singleLine = true
          )
          OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date") },
            modifier = Modifier.weight(1.5f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = instructions,
          onValueChange = { instructions = it },
          label = { Text("Detailed Instructions & Guidelines *") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = classGrade,
            onValueChange = { classGrade = it.filter { c -> c.isDigit() } },
            label = { Text("Class *") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = division,
            onValueChange = { division = it.uppercase(Locale.US).take(2) },
            label = { Text("Division *") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = maxPoints,
            onValueChange = { maxPoints = it.filter { c -> c.isDigit() }.take(2) },
            label = { Text("Max Marks") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = if (selectedFileName.isEmpty()) "No file attached" else selectedFileName,
          onValueChange = {},
          readOnly = true,
          isError = attachmentError != null,
          supportingText = {
            if (attachmentError != null) {
              Text(attachmentError.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
          },
          label = { Text("Attach Reference File (PDF / DOC / DOCX)") },
          trailingIcon = {
            if (isUploading) {
              CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
              IconButton(onClick = { filePicker.launch("*/*") }, enabled = !isUploading) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
              }
            }
          },
          leadingIcon = {
            if (selectedFileName.isNotEmpty() && !isUploading) {
              IconButton(onClick = {
                selectedFileName = ""
                selectedFileMime = ""
                selectedFileUri = null
              }) {
                Icon(Icons.Default.Clear, contentDescription = "Remove Attachment")
              }
            }
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            attachmentError = null
            val validClass = classGrade.trim().isNotEmpty()
            val validDivision = division.trim().isNotEmpty()
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val parsedDueDate = runCatching { sdf.parse(dueDate.trim()) }.getOrNull()
            val todayMidnight = runCatching { sdf.parse(todayDate) }.getOrNull()
            if (parsedDueDate != null && todayMidnight != null && parsedDueDate.before(todayMidnight)) {
              attachmentError = "Due date cannot be in the past."
              return@Button
            }

            if (title.isNotBlank() && instructions.isNotBlank() && validClass && validDivision) {
              isUploading = true
              createScope.launch {
                var attachedName = selectedFileName
                var attachedMime = selectedFileMime
                var attachedUrl = ""
                try {
                  val pickUri = selectedFileUri
                  if (pickUri != null && selectedFileName.isNotEmpty()) {
                    val result = repository.uploadAttachment(
                      context = context,
                      folder = "assignments",
                      sourceUri = pickUri,
                      displayName = selectedFileName,
                      mimeType = selectedFileMime
                    )
                    attachedName = result.name
                    attachedMime = result.mimeType
                    attachedUrl = result.url
                  }
                  val assignment = HomeworkAssignment(
                    id = "hw_${System.currentTimeMillis()}",
                    title = title.trim(),
                    subject = subject.trim(),
                    classGrade = classGrade.trim(),
                    division = division.trim(),
                    teacherName = teacherName,
                    assignedDate = todayDate,
                    dueDate = dueDate.trim(),
                    instructions = instructions.trim(),
                    attachmentName = attachedName,
                    attachmentType = attachedMime,
                    attachmentUrl = attachedUrl,
                    maxPoints = maxPoints.toIntOrNull() ?: 20,
                    isCompletedByStudent = false,
                    submissionStatus = "Pending"
                  )
                  onCreate(assignment)
                  onDismiss()
                } catch (e: Exception) {
                  attachmentError = e.message ?: "Could not upload attachment."
                } finally {
                  isUploading = false
                }
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = title.isNotBlank() && instructions.isNotBlank() &&
            classGrade.isNotBlank() && division.isNotBlank() && !isUploading
        ) {
          Icon(Icons.Default.Assignment, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Assign to Class $classGrade-$division")
        }
      }
    }
  }
}

@Composable
fun ApplyLeaveDialog(
  applicantName: String,
  applicantRole: UserRole,
  onDismiss: () -> Unit,
  onApply: (leaveType: String, start: String, end: String, days: Int, reason: String) -> Unit
) {
  var leaveType by remember { mutableStateOf("Medical Leave") }
  var startDate by remember { mutableStateOf("01 Sep 2026") }
  var endDate by remember { mutableStateOf("02 Sep 2026") }
  var daysCount by remember { mutableStateOf("2") }
  var reason by remember { mutableStateOf("") }
  var dateError by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Apply for Leave",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Leave Type",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Medical", "Casual", "Family Event", "Sports").forEach { type ->
            FilterChip(
              selected = leaveType.contains(type),
              onClick = { leaveType = "$type Leave" },
              label = { Text(type, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("From Date") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("To Date") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        if (dateError != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = dateError!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Reason for Absence *") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            dateError = null
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val start = runCatching { sdf.parse(startDate.trim()) }.getOrNull()
            val end = runCatching { sdf.parse(endDate.trim()) }.getOrNull()
            if (start != null && end != null && end.before(start)) {
              dateError = "End date must be on or after start date."
              return@Button
            }
            if (reason.isNotBlank()) {
              onApply(
                leaveType,
                startDate,
                endDate,
                daysCount.toIntOrNull() ?: 1,
                reason.trim()
              )
              onDismiss()
            }
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = reason.isNotBlank()
        ) {
          Icon(Icons.Default.Send, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Submit Leave Application")
        }
      }
    }
  }
}

@Composable
fun FeePaymentDialog(
  studentName: String,
  amountDue: Long,
  isProcessing: Boolean,
  paymentCompleted: Boolean,
  razorpayPaymentId: String,
  onDismiss: () -> Unit,
  onStartRazorpay: (method: String) -> Unit
) {
  var selectedMethod by remember { mutableStateOf("UPI (Google Pay / PhonePe)") }
  var generatedReceiptNo by remember { mutableStateOf("REC-2026-${(1000..9999).random()}") }

  Dialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .glassEffect(cornerRadius = 24.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        if (!paymentCompleted) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Pay School Fees",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Student: $studentName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Razorpay Test Mode Banner
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFF3CD),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = Color(0xFF856404),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Razorpay Test Mode â€” No real money will be charged",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF856404)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = RevenexPrimaryContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Total Payable Amount",
                  style = MaterialTheme.typography.labelMedium,
                  color = RevenexOnPrimaryContainer
                )
                Text(
                  text = "₹${String.format(java.util.Locale.US, "%,d", amountDue / 100)}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = RevenexOnPrimaryContainer
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.8f)
              ) {
                Text(
                  text = "Instant Receipt",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = RevenexBlue,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Select Payment Gateway / Method",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(8.dp))

          listOf(
            "UPI (Google Pay / PhonePe / Paytm)" to Icons.Default.QrCodeScanner,
            "Debit / Credit Card (Visa / MasterCard)" to Icons.Default.CreditCard,
            "Net Banking (All Major Indian Banks)" to Icons.Default.AccountBalance
          ).forEach { (method, icon) ->
            val isSelected = selectedMethod.startsWith(method.take(5))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedMethod = method }
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = method,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.weight(1f)
                )
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedMethod = method }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          if (isProcessing) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Securing transaction via Razorpay (Test Mode)...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          } else {
            Button(
              onClick = {
                onStartRazorpay(selectedMethod)
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_fee_payment_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Pay â‚¹${String.format(java.util.Locale.US, "%,.0f", amountDue)} Securely",
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else {
          // ========== Success / Receipt State ==========
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(StatusSuccessContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Payment Received Successfully!",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = StatusSuccessText
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Receipt details card
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                ReceiptRow("Receipt No.", generatedReceiptNo)
                ReceiptRow("Razorpay ID", razorpayPaymentId)
                ReceiptRow("Student", studentName)
                ReceiptRow("Amount", "â‚¹${String.format(java.util.Locale.US, "%,.0f", amountDue)}")
                ReceiptRow("Method", selectedMethod.substringBefore(" ("))
                ReceiptRow("Status", "SUCCESS âœ“")
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "SMS & Email confirmation sent to registered contact.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = onDismiss,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
            ) {
              Text("Done & View Updated Ledger", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ReceiptRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold
    )
  }
}

