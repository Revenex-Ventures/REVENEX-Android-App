package com.example.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeStatus
import com.example.data.model.UserRole
import com.example.data.model.Student
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleProgressRing
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StudentDetailScreen(
  studentId: String,
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onPayFee: (studentName: String, amountDue: Long) -> Unit
) {
  val students by repository.students.collectAsState()
  val feeRecords by repository.feeRecords.collectAsState()
  val reportCards by repository.reportCards.collectAsState()
  val currentUser by repository.currentUser.collectAsState()

  val student = students.firstOrNull { it.id == studentId } ?: students.first()
  val feeRecord = feeRecords.firstOrNull { it.studentId == student.id }
  val reportCard = reportCards.firstOrNull { it.studentId == student.id }

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Profile", "Academics", "Attendance", "Fee Ledger")

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
            text = "Student 360 Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
          )

          if (currentUser.role == UserRole.PRINCIPAL) {
            IconButton(
              onClick = { showEditDialog = true },
              modifier = Modifier.testTag("btn_edit_student")
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Student")
            }
            IconButton(
              onClick = { showDeactivateConfirm = true },
              modifier = Modifier.testTag("btn_deactivate_student")
            ) {
              Icon(Icons.Default.PersonRemove, contentDescription = "Deactivate Student", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  ) { paddingValues ->
    // Modals
    if (showDeactivateConfirm) {
      AlertDialog(
        onDismissRequest = { showDeactivateConfirm = false },
        title = { Text("Deactivate Student Profile?", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to deactivate ${student.name}'s profile? They will no longer appear in active classroom rosters or fee sheets.") },
        confirmButton = {
          Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = {
              repository.updateStudent(student.copy(status = "INACTIVE"))
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
      var editName by remember { mutableStateOf(student.name) }
      var editClass by remember { mutableStateOf(student.classGrade) }
      var editDiv by remember { mutableStateOf(student.division) }
      var editDob by remember { mutableStateOf(student.dob) }
      var editParent by remember { mutableStateOf(student.parentName) }
      var editPhone by remember { mutableStateOf(student.parentPhone) }
      var editEmail by remember { mutableStateOf(student.parentEmail) }
      var editAddress by remember { mutableStateOf(student.address) }

      AlertDialog(
        onDismissRequest = { showEditDialog = false },
        title = { Text("Edit Student Details", fontWeight = FontWeight.Bold) },
        text = {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Student Full Name") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = editClass,
                  onValueChange = { editClass = it },
                  label = { Text("Class") },
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = editDiv,
                  onValueChange = { editDiv = it },
                  label = { Text("Division") },
                  modifier = Modifier.weight(1f)
                )
              }
            }
            item {
              OutlinedTextField(
                value = editDob,
                onValueChange = { editDob = it },
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editParent,
                onValueChange = { editParent = it },
                label = { Text("Father / Guardian Name") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                label = { Text("Guardian Contact Phone") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text("Guardian Email") },
                modifier = Modifier.fillMaxWidth()
              )
            }
            item {
              OutlinedTextField(
                value = editAddress,
                onValueChange = { editAddress = it },
                label = { Text("Residential Address") },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              repository.updateStudent(
                student.copy(
                  name = editName.trim(),
                  classGrade = editClass.trim(),
                  division = editDiv.trim(),
                  dob = editDob.trim(),
                  parentName = editParent.trim(),
                  parentPhone = editPhone.trim(),
                  parentEmail = editEmail.trim(),
                  address = editAddress.trim()
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
        .testTag("student_detail_view"),
      contentPadding = PaddingValues(bottom = 60.dp)
    ) {
      // Top Identity Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = RevenexPrimary)
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
                    text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
              Spacer(modifier = Modifier.width(16.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = student.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "Class ${student.fullClass} • Roll #${student.rollNumber}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                  text = "Admission ID: ${student.admissionNumber}",
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
                Text("BLOOD GROUP", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(student.bloodGroup, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("ATTENDANCE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${student.attendancePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("ACADEMIC GPA", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${student.gpa} / 10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Column {
                Text("FEE STATUS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(student.feeStatus.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (student.feeStatus == FeeStatus.PAID) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
              }
            }
          }
        }
      }

      // Tab Row
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          modifier = Modifier.fillMaxWidth()
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = { Text(title, fontWeight = FontWeight.SemiBold) }
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Tab 1: Profile & Guardian
      if (selectedTab == 0) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            InfoCard(title = "Primary Guardian", value = student.parentName, subtitle = "Father / Legal Guardian", icon = Icons.Default.FamilyRestroom)
            InfoCard(title = "Contact Phone", value = student.parentPhone, subtitle = "Registered for SMS/WhatsApp alerts", icon = Icons.Default.Phone)
            InfoCard(title = "Parent Email", value = student.parentEmail, subtitle = "Official billing & circular email", icon = Icons.Default.Email)
            InfoCard(title = "Residential Address", value = student.address, subtitle = "Pune City Jurisdiction", icon = Icons.Default.Home)
            InfoCard(title = "Admission Date", value = student.admissionDate, subtitle = "Enrolled via Central Board Entrance", icon = Icons.Default.CalendarToday)
          }
        }
      }

      // Tab 2: Academics & Report Card
      if (selectedTab == 1) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (reportCard != null) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = reportCard.term,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "Rank #${reportCard.rankInClass} / ${reportCard.totalStudents}",
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  reportCard.scores.forEach { score ->
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = score.subjectName,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.SemiBold
                        )
                        Text(
                          text = score.remarks,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          fontSize = 11.sp
                        )
                      }
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RevenexPrimaryContainer
                      ) {
                        Text(
                          text = "${score.obtainedMarks}/${score.maxMarks} (${score.grade})",
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold,
                          color = RevenexBlue,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  }

                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "Principal's Remark:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "\"${reportCard.principalRemark}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            } else {
              Text("No published report card yet for this term.", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }

      // Tab 3: Attendance
      if (selectedTab == 2) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            SimpleProgressRing(
              percentage = student.attendancePercent,
              title = "Term Attendance Ratio",
              subtitle = "Present in 128 out of 135 working days this academic session.",
              ringColor = StatusSuccess
            )

            InfoCard(title = "Present Days", value = "128 Days", subtitle = "Regular physical attendance", icon = Icons.Default.CheckCircle)
            InfoCard(title = "Excused Medical Leave", value = "5 Days", subtitle = "Doctor certificate submitted & approved", icon = Icons.Default.LocalHospital)
            InfoCard(title = "Unexcused Absence", value = "2 Days", subtitle = "Permitted buffer limit", icon = Icons.Default.Cancel)
          }
        }
      }

      // Tab 4: Fee Ledger
      if (selectedTab == 3) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Annual Tuition Fee", style = MaterialTheme.typography.bodyMedium)
                  Text("₹42,000", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Examination & Lab Fee", style = MaterialTheme.typography.bodyMedium)
                  Text("₹10,500", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Bus Transport Fee", style = MaterialTheme.typography.bodyMedium)
                  Text("₹12,000", fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.padding(vertical = 10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Total Annual Fee", fontWeight = FontWeight.Bold)
                  Text("₹64,500", fontWeight = FontWeight.Bold)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Paid Till Date", color = StatusSuccessText, fontWeight = FontWeight.SemiBold)
                  Text("₹${(feeRecord?.paidAmount ?: 5000000L) / 100}", color = StatusSuccessText, fontWeight = FontWeight.Bold)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Pending Due Balance", color = StatusErrorText, fontWeight = FontWeight.SemiBold)
                  Text("₹${(feeRecord?.pendingAmount ?: student.feePendingAmount) / 100}", color = StatusErrorText, fontWeight = FontWeight.Bold)
                }

                if (student.feePendingAmount > 0L) {
                  Spacer(modifier = Modifier.height(16.dp))
                  Button(
                    onClick = { onPayFee(student.name, student.feePendingAmount) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
                  ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Outstanding Dues (₹${student.feePendingAmount / 100})")
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun InfoCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
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
          .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          fontSize = 11.sp
        )
      }
    }
  }
}
