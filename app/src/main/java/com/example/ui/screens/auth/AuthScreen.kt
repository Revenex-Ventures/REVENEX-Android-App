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
 * Supabase Mode: email/password with role resolved from the protected backend profile.
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

  var principalEmail by remember { mutableStateOf("") }
  var principalPassword by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 560.dp)
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = Spacing.lg16, vertical = Spacing.lg16)
        .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Header Brand Crest
      RevenexLogoCrest(
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "REVENEX SCHOOL ERP",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = 1.sp
      )

      Text(
        text = "One Digital Operating System For The Entire School",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Mode Switcher Banner (Demo vs Production)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
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
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = dataMode.label,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )
          }
          FilterChip(
            enabled = !isLoggingIn,
            selected = dataMode == DataMode.SUPABASE_DEMO,
            onClick = {
              val nextMode = if (dataMode == DataMode.DEMO) DataMode.SUPABASE_DEMO else DataMode.DEMO
              repository.setDataMode(nextMode)
              errorMessage = null
            },
            label = {
              Text(
                text = if (dataMode == DataMode.DEMO) "Use Supabase" else "Local demo",
                fontSize = 12.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              labelColor = MaterialTheme.colorScheme.onSurface,
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Login Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                repository.switchRole(selectedRole) {
                  isLoggingIn = false
                  onLoginSuccess()
                }
              }
            )
          } else if (dataMode == DataMode.SUPABASE_DEMO) {
            Text("Supabase school demo", style = MaterialTheme.typography.titleMedium)
            Text("Use your assigned school account. Your role is loaded securely after sign-in.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value=principalEmail,onValueChange={ principalEmail=it;errorMessage=null },label={Text("Email")},singleLine=true,enabled=!isLoggingIn,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=principalPassword,onValueChange={ principalPassword=it;errorMessage=null },label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true,enabled=!isLoggingIn,modifier=Modifier.fillMaxWidth())
            errorMessage?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall) }
            Button(onClick={
              isLoggingIn=true
              repository.loginWithSupabase(principalEmail,principalPassword) { ok,error ->
                isLoggingIn=false
                if(ok) { principalPassword="";onLoginSuccess() } else errorMessage=error
              }
            },enabled=!isLoggingIn && principalEmail.isNotBlank() && principalPassword.isNotBlank(),modifier=Modifier.fillMaxWidth()) {
              Text(if(isLoggingIn) "Signing in…" else "Sign in")
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Revenex School ERP • Demonstration build",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        textAlign = TextAlign.Center
      )
    }
  }
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

  val demoRoles = listOf(UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.TEACHER, UserRole.STUDENT)

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    demoRoles.forEach { role ->
      val isSelected = selectedRole == role
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
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
              UserRole.ADMIN -> Icons.Default.ManageAccounts
              UserRole.TEACHER -> Icons.Default.School
              else -> Icons.Default.FamilyRestroom
            },
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = when (role) {
                UserRole.PRINCIPAL -> "Principal / Academic Leader"
                UserRole.ADMIN -> "School Administrator"
                UserRole.TEACHER -> "Teacher / Faculty"
                else -> "Student / Parent Portal"
              },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = when (role) {
                UserRole.PRINCIPAL -> "Dr. Arvind Sharma (Academic Director)"
                UserRole.ADMIN -> "Meera Iyer (School Operations)"
                UserRole.TEACHER -> "Prof. Sunita Rao (Math Faculty)"
                else -> "Aarav Patel (Class 10-A) / Parent Portal"
              },
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          if (isSelected) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Selected",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
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

