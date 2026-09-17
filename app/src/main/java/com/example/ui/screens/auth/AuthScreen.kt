package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.RevenexLogoCrest
import com.example.ui.theme.*

/**
 * Authentication entry point for REVENEX School ERP.
 *
 * Production Mode:
 *   - Principal: Google Workspace Sign-In (verified against allowlist)
 *   - Teacher: Employee ID + Date of Birth
 *   - Student/Parent: No independent login (parent accesses via guardian link)
 *
 * Demo Mode:
 *   - Quick role selector with instant access for testing all personas
 */
@Composable
fun AuthScreen(
  repository: ErpDataRepository,
  onLoginSuccess: () -> Unit
) {
  val dataMode by repository.dataMode.collectAsState()
  var isLoggingIn by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Demo mode state
  var selectedRole by remember { mutableStateOf(UserRole.PRINCIPAL) }

  // Production mode: which login tab is active
  var loginTab by remember { mutableStateOf(LoginTab.PRINCIPAL) }

  // Teacher login fields
  var teacherEmployeeId by remember { mutableStateOf("") }
  var teacherDob by remember { mutableStateOf("") }

  // Student login fields
  var studentAdmissionNo by remember { mutableStateOf("") }
  var studentDob by remember { mutableStateOf("") }

  // Principal Google Sign-In fields (fallback email/pass until real Google integration)
  var principalEmail by remember { mutableStateOf("") }
  var principalPassword by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ScholaSlateNavyDark)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Header Brand Crest
      RevenexLogoCrest(
        modifier = Modifier.size(70.dp),
        tint = RevenexGoldLight
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "REVENEX SCHOOL ERP",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        letterSpacing = 1.sp
      )

      Text(
        text = "One Digital Operating System For The Entire School",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Mode Switcher Banner (Demo vs Production)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (dataMode == DataMode.DEMO) Icons.Default.Science else Icons.Default.CloudSync,
              contentDescription = null,
              tint = RevenexGoldLight,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = dataMode.label,
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
          FilterChip(
            selected = dataMode == DataMode.PRODUCTION,
            onClick = {
              val nextMode = if (dataMode == DataMode.DEMO) DataMode.PRODUCTION else DataMode.DEMO
              repository.setDataMode(nextMode)
              errorMessage = null
            },
            label = {
              Text(
                text = if (dataMode == DataMode.DEMO) "Switch to Live Cloud" else "Switch to Demo Mode",
                fontSize = 11.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = RevenexGold,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Login Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          if (dataMode == DataMode.DEMO) {
            // ========== DEMO MODE: Quick Role Selector ==========
            DemoModeLoginContent(
              selectedRole = selectedRole,
              onRoleSelected = { role ->
                // Merge PARENT → STUDENT in demo mode (same portal)
                selectedRole = if (role == UserRole.PARENT) UserRole.STUDENT else role
                errorMessage = null
              },
              isLoggingIn = isLoggingIn,
              errorMessage = errorMessage,
              onLogin = {
                isLoggingIn = true
                repository.switchRole(selectedRole)
                isLoggingIn = false
                onLoginSuccess()
              }
            )
          } else {
            // ========== PRODUCTION MODE: Role-Specific Login ==========
            ProductionModeLoginContent(
              loginTab = loginTab,
              onTabChanged = { loginTab = it; errorMessage = null },
              principalEmail = principalEmail,
              onPrincipalEmailChanged = { principalEmail = it; errorMessage = null },
              principalPassword = principalPassword,
              onPrincipalPasswordChanged = { principalPassword = it; errorMessage = null },
              teacherEmployeeId = teacherEmployeeId,
              onTeacherEmployeeIdChanged = { teacherEmployeeId = it; errorMessage = null },
              teacherDob = teacherDob,
              onTeacherDobChanged = { teacherDob = it; errorMessage = null },
              studentAdmissionNo = studentAdmissionNo,
              onStudentAdmissionNoChanged = { studentAdmissionNo = it; errorMessage = null },
              studentDob = studentDob,
              onStudentDobChanged = { studentDob = it; errorMessage = null },
              isLoggingIn = isLoggingIn,
              errorMessage = errorMessage,
              onPrincipalGoogleSignIn = {
                // Google Sign-In flow (fallback to Firebase email/pass for now)
                if (principalEmail.isBlank()) {
                  errorMessage = "Please enter your Google Workspace email."
                  return@ProductionModeLoginContent
                }
                isLoggingIn = true
                repository.loginWithFirebase(principalEmail, principalPassword) { success, err ->
                  isLoggingIn = false
                  if (success) {
                    onLoginSuccess()
                  } else {
                    errorMessage = err ?: "Authentication failed. Verify your Google Workspace credentials."
                  }
                }
              },
              onTeacherLogin = {
                if (teacherEmployeeId.isBlank() || teacherDob.isBlank()) {
                  errorMessage = "Please enter both Employee ID and Date of Birth."
                  return@ProductionModeLoginContent
                }
                isLoggingIn = true
                val normalizedDob = teacherDob.trim().replace(Regex("[-/\\s]"), "")
                val email = "${teacherEmployeeId.trim().lowercase()}@revenex.edu.in"
                repository.loginWithFirebase(email, normalizedDob) { success, err ->
                  isLoggingIn = false
                  if (success) {
                    onLoginSuccess()
                  } else {
                    errorMessage = err ?: "Login failed. Verify Employee ID and DOB."
                  }
                }
              },
              onStudentLogin = {
                if (studentAdmissionNo.isBlank() || studentDob.isBlank()) {
                  errorMessage = "Please enter both Student ID (Admission Number) and Date of Birth."
                  return@ProductionModeLoginContent
                }
                isLoggingIn = true
                val normalizedDob = studentDob.trim().replace(Regex("[-/\\s]"), "")
                val email = "${studentAdmissionNo.trim().lowercase()}@revenex.edu.in"
                repository.loginWithFirebase(email, normalizedDob) { success, err ->
                  isLoggingIn = false
                  if (success) {
                    onLoginSuccess()
                  } else {
                    errorMessage = err ?: "Login failed. Verify Admission Number and DOB."
                  }
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Info banner
      if (dataMode == DataMode.PRODUCTION) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White.copy(alpha = 0.10f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = RevenexGoldLight,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Students: Use your Admission Number as the Student ID. " +
                "Date of Birth in DD/MM/YYYY format. Contact admin if you don't have these.",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f),
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Revenex Cloud Enterprise v4.2 • Protected with TLS 1.3 & Role Authorization",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.65f),
        textAlign = TextAlign.Center
      )
    }
  }
}

// ========================================================================
// Login Tabs for Production Mode
// ========================================================================

enum class LoginTab(val label: String, val icon: @Composable () -> Unit) {
  PRINCIPAL(
    "Principal",
    { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) }
  ),
  TEACHER(
    "Teacher",
    { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) }
  ),
  STUDENT(
    "Student",
    { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
  )
}

// ========================================================================
// Demo Mode Login Content
// ========================================================================

@Composable
private fun DemoModeLoginContent(
  selectedRole: UserRole,
  onRoleSelected: (UserRole) -> Unit,
  isLoggingIn: Boolean,
  errorMessage: String?,
  onLogin: () -> Unit
) {
  Text(
    text = "Demo Mode — Select Persona to Explore",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold
  )

  Spacer(modifier = Modifier.height(10.dp))

  // Show only 3 roles: Principal, Teacher, Student/Parent unified
  val demoRoles = listOf(UserRole.PRINCIPAL, UserRole.TEACHER, UserRole.STUDENT)

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    demoRoles.forEach { role ->
      val isSelected = selectedRole == role
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) RevenexPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .clickable { onRoleSelected(role) }
          .testTag("role_select_${role.name.lowercase()}")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = when (role) {
              UserRole.PRINCIPAL -> Icons.Default.AdminPanelSettings
              UserRole.TEACHER -> Icons.Default.School
              else -> Icons.Default.FamilyRestroom
            },
            contentDescription = null,
            tint = if (isSelected) RevenexBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = when (role) {
                UserRole.PRINCIPAL -> "Principal / Administrator"
                UserRole.TEACHER -> "Teacher / Faculty"
                else -> "Student / Parent Portal"
              },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) RevenexBlue else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = when (role) {
                UserRole.PRINCIPAL -> "Dr. Arvind Sharma (Principal & Admin)"
                UserRole.TEACHER -> "Prof. Sunita Rao (Math Faculty)"
                else -> "Aarav Patel (Class 10-A) / Parent Portal"
              },
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Selected",
              tint = RevenexBlue,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }

  if (errorMessage != null) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = errorMessage,
      color = MaterialTheme.colorScheme.error,
      style = MaterialTheme.typography.bodySmall
    )
  }

  Spacer(modifier = Modifier.height(18.dp))

  if (isLoggingIn) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(modifier = Modifier.size(36.dp))
    }
  } else {
    Button(
      onClick = onLogin,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("submit_login_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
    ) {
      Icon(Icons.Default.Login, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Enter ${selectedRole.displayName} Dashboard",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

// ========================================================================
// Production Mode Login Content
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductionModeLoginContent(
  loginTab: LoginTab,
  onTabChanged: (LoginTab) -> Unit,
  principalEmail: String,
  onPrincipalEmailChanged: (String) -> Unit,
  principalPassword: String,
  onPrincipalPasswordChanged: (String) -> Unit,
  teacherEmployeeId: String,
  onTeacherEmployeeIdChanged: (String) -> Unit,
  teacherDob: String,
  onTeacherDobChanged: (String) -> Unit,
  studentAdmissionNo: String,
  onStudentAdmissionNoChanged: (String) -> Unit,
  studentDob: String,
  onStudentDobChanged: (String) -> Unit,
  isLoggingIn: Boolean,
  errorMessage: String?,
  onPrincipalGoogleSignIn: () -> Unit,
  onTeacherLogin: () -> Unit,
  onStudentLogin: () -> Unit
) {
  Text(
    text = "Sign in to Revenex ERP",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold
  )

  Text(
    text = "Choose your role and authenticate",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )

  Spacer(modifier = Modifier.height(14.dp))

  // Tab Selector: Principal | Teacher | Student
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    LoginTab.values().forEach { tab ->
      val isActive = loginTab == tab
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) RevenexBlue else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onTabChanged(tab) }
          .testTag("login_tab_${tab.name.lowercase()}")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = when (tab) {
              LoginTab.PRINCIPAL -> Icons.Default.AdminPanelSettings
              LoginTab.TEACHER -> Icons.Default.School
              LoginTab.STUDENT -> Icons.Default.FamilyRestroom
            },
            contentDescription = null,
            tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  Spacer(modifier = Modifier.height(16.dp))

  // Tab Content
  AnimatedContent(
    targetState = loginTab,
    transitionSpec = {
      fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
    },
    label = "login_tab_content"
  ) { tab ->
    Column {
      when (tab) {
        LoginTab.STUDENT -> {
          // Student / Parent Portal: Admission Number + Date of Birth
          Text(
            text = "Student / Parent Portal",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Enter your Admission Number and Date of Birth",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = studentAdmissionNo,
            onValueChange = onStudentAdmissionNoChanged,
            label = { Text("Student ID / Admission Number") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            placeholder = { Text("e.g., REV-2026-001") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_student_id"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = studentDob,
            onValueChange = onStudentDobChanged,
            label = { Text("Date of Birth") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            placeholder = { Text("DD/MM/YYYY") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_student_dob"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onStudentLogin,
            enabled = !isLoggingIn,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_student_signin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ScholaTerracotta)
          ) {
            if (isLoggingIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ScholaSlateNavyDark,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Enter Student Portal",
              fontWeight = FontWeight.Bold
            )
          }
        }

        LoginTab.PRINCIPAL -> {
          // Principal: Google Workspace Sign-In
          Text(
            text = "Principal / Administrator",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Sign in with your Google Workspace account",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = principalEmail,
            onValueChange = onPrincipalEmailChanged,
            label = { Text("Google Workspace Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            placeholder = { Text("principal@school.edu.in") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_principal_email"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = principalPassword,
            onValueChange = onPrincipalPasswordChanged,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_principal_password"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onPrincipalGoogleSignIn,
            enabled = !isLoggingIn,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_principal_signin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
          ) {
            if (isLoggingIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.VpnKey, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sign in with Google Workspace",
              fontWeight = FontWeight.Bold
            )
          }
        }

        LoginTab.TEACHER -> {
          // Teacher: Employee ID + Date of Birth
          Text(
            text = "Faculty / Teacher",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Enter your Employee ID and Date of Birth",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = teacherEmployeeId,
            onValueChange = onTeacherEmployeeIdChanged,
            label = { Text("Employee ID") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            placeholder = { Text("e.g., EMP-201") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_teacher_id"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = teacherDob,
            onValueChange = onTeacherDobChanged,
            label = { Text("Date of Birth") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            placeholder = { Text("DD/MM/YYYY") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_teacher_dob"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onTeacherLogin,
            enabled = !isLoggingIn,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("btn_teacher_signin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RevenexBlue)
          ) {
            if (isLoggingIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Verify & Sign In",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }

  // Error display
  if (errorMessage != null) {
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = MaterialTheme.colorScheme.errorContainer,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }
    }
  }
}
