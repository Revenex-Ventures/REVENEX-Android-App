package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.AnimatedFadeIn
import com.example.ui.components.SectionHeader
import com.example.ui.theme.RevenexBlue
import com.example.ui.theme.RevenexPrimary
import com.example.ui.theme.RevenexPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onNavigateTo: (String) -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  val students by repository.students.collectAsState()
  val teachers by repository.teachers.collectAsState()

  val studentProfile = remember(students, currentUser) {
    students.firstOrNull { it.id == currentUser.associatedStudentId || it.name.equals(currentUser.name, ignoreCase = true) }
  }

  val teacherProfile = remember(teachers, currentUser) {
    teachers.firstOrNull { it.email.equals(currentUser.email, ignoreCase = true) || it.name.equals(currentUser.name, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("My Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("profile_screen_list"),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      // Header Section: Profile Card with Avatar
      item {
        AnimatedFadeIn(delayMillis = 0) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
              .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Surface(
                shape = CircleShape,
                color = when (currentUser.role) {
                  UserRole.PRINCIPAL -> RevenexBlue
                  UserRole.TEACHER -> Color(0xFF6D28D9)
                  else -> Color(0xFF15803D)
                },
                modifier = Modifier.size(100.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = currentUser.avatarInitials.ifBlank { "U" },
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = currentUser.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = currentUser.designation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (currentUser.role) {
                  UserRole.PRINCIPAL -> RevenexPrimaryContainer
                  UserRole.TEACHER -> Color(0xFFEDE9FE)
                  else -> Color(0xFFDCFCE7)
                }
              ) {
                Text(
                  text = currentUser.role.displayName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = when (currentUser.role) {
                    UserRole.PRINCIPAL -> RevenexBlue
                    UserRole.TEACHER -> Color(0xFF6D28D9)
                    else -> Color(0xFF15803D)
                  },
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }

      // Role-Specific Profile Information Cards
      when (currentUser.role) {
        UserRole.PRINCIPAL -> {
          item {
            AnimatedFadeIn(delayMillis = 100) {
              Column(modifier = Modifier.padding(16.dp)) {
                ProfileSectionHeader(title = "Contact & Authentication")
                InfoRow(label = "Email Address", value = currentUser.email, icon = Icons.Default.Email)
                InfoRow(label = "Phone Number", value = currentUser.phone, icon = Icons.Default.Phone)
                InfoRow(label = "Designation", value = currentUser.designation, icon = Icons.Default.Badge)
                InfoRow(label = "School ID", value = currentUser.schoolId, icon = Icons.Default.School)

                Spacer(modifier = Modifier.height(20.dp))
                ProfileSectionHeader(title = "Administrative Access Directories")
                
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo("students") },
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = CardDefaults.outlinedCardBorder()
                ) {
                  Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = RevenexBlue)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text("Student Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                      Text("Manage student enrollments, fee records, & academic logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo("teachers") },
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = CardDefaults.outlinedCardBorder()
                ) {
                  Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF6D28D9))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text("Faculty Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                      Text("Manage teacher assignments, periods, & class responsibilities", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                  }
                }
              }
            }
          }
        }

        UserRole.TEACHER -> {
          item {
            AnimatedFadeIn(delayMillis = 100) {
              Column(modifier = Modifier.padding(16.dp)) {
                ProfileSectionHeader(title = "Faculty Credentials")
                val t = teacherProfile
                InfoRow(label = "Employee ID", value = t?.employeeId ?: currentUser.id, icon = Icons.Default.Badge)
                InfoRow(label = "Subject Specialization", value = t?.subjects?.joinToString(", ") ?: "Mathematics", icon = Icons.Default.Book)
                InfoRow(label = "Assigned Classes", value = t?.assignedClasses?.joinToString(", ") ?: "10-A", icon = Icons.Default.Class)
                InfoRow(label = "Department", value = t?.department ?: "Mathematics Department", icon = Icons.Default.CorporateFare)

                Spacer(modifier = Modifier.height(16.dp))
                ProfileSectionHeader(title = "Employment Info")
                InfoRow(label = "Joining Date", value = t?.joiningDate ?: "01/06/2021", icon = Icons.Default.CalendarToday)
                InfoRow(label = "Experience", value = "${t?.experienceYears ?: 5} Years", icon = Icons.Default.Timeline)
                InfoRow(label = "Weekly Period Load", value = "${t?.weeklyPeriods ?: 28} Periods", icon = Icons.Default.HourglassEmpty)
                InfoRow(label = "Attendance Percentage", value = "${t?.attendancePercent ?: 98.5}%", icon = Icons.Default.FactCheck)

                Spacer(modifier = Modifier.height(16.dp))
                ProfileSectionHeader(title = "Contact Information")
                InfoRow(label = "Email Address", value = t?.email ?: currentUser.email, icon = Icons.Default.Email)
                InfoRow(label = "Phone Number", value = t?.phone ?: currentUser.phone, icon = Icons.Default.Phone)
                InfoRow(label = "Date of Birth", value = t?.dob ?: "15/08/1985", icon = Icons.Default.Cake)
                InfoRow(label = "Gender", value = t?.gender ?: "Female", icon = Icons.Default.Person)
              }
            }
          }
        }

        UserRole.STUDENT, UserRole.PARENT -> {
          item {
            AnimatedFadeIn(delayMillis = 100) {
              Column(modifier = Modifier.padding(16.dp)) {
                ProfileSectionHeader(title = "Student Identification")
                val s = studentProfile
                InfoRow(label = "Student ID", value = s?.id ?: currentUser.associatedStudentId, icon = Icons.Default.Badge)
                InfoRow(label = "Admission Number", value = s?.admissionNumber ?: "REV-2026-001", icon = Icons.Default.Fingerprint)
                InfoRow(label = "Current Class Grade", value = s?.fullClass ?: "10-A", icon = Icons.Default.Class)
                InfoRow(label = "Roll Number", value = s?.rollNumber?.toString() ?: "12", icon = Icons.Default.FormatListNumbered)
                InfoRow(label = "GPA Score", value = "${s?.gpa ?: 9.2} / 10.0", icon = Icons.Default.Grade)

                Spacer(modifier = Modifier.height(16.dp))
                ProfileSectionHeader(title = "Guardian & Family Information")
                InfoRow(label = "Father / Guardian", value = s?.parentName ?: "Rajesh Patel", icon = Icons.Default.FamilyRestroom)
                InfoRow(label = "Guardian Phone", value = s?.parentPhone ?: "+91 98220 54199", icon = Icons.Default.Phone)
                InfoRow(label = "Guardian Email", value = s?.parentEmail ?: "rajesh.patel.biz@gmail.com", icon = Icons.Default.Email)
                InfoRow(label = "Residential Address", value = s?.address ?: "102, Shanti Kunj, Sector 15, Dwarka, New Delhi - 110075", icon = Icons.Default.Home)

                Spacer(modifier = Modifier.height(16.dp))
                ProfileSectionHeader(title = "Personal & Health Records")
                InfoRow(label = "Date of Birth", value = s?.dob ?: "12/10/2010", icon = Icons.Default.Cake)
                InfoRow(label = "Gender", value = s?.gender ?: "Male", icon = Icons.Default.Person)
                InfoRow(label = "Blood Group", value = s?.bloodGroup ?: "O+", icon = Icons.Default.Bloodtype)
                InfoRow(label = "Admission Date", value = s?.admissionDate ?: "02/04/2020", icon = Icons.Default.CalendarToday)
                InfoRow(label = "Attendance Health Rate", value = "${s?.attendancePercent ?: 95.0}%", icon = Icons.Default.EventAvailable)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(vertical = 8.dp)
  )
}

@Composable
private fun InfoRow(
  label: String,
  value: String,
  icon: ImageVector
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
