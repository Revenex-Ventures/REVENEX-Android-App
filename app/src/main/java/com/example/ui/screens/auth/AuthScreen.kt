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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(repository: ErpDataRepository, onLoginSuccess: () -> Unit) {
  val mode by repository.dataMode.collectAsState()
  var isLoggingIn by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(UserRole.PRINCIPAL) }
  var demoSheet by remember { mutableStateOf(false) }
  var help by remember { mutableStateOf(false) }
  val signIn: () -> Unit = {
    if (!isLoggingIn && email.isNotBlank() && password.isNotBlank()) {
      isLoggingIn = true; error = null
      if (mode != DataMode.SUPABASE_DEMO) repository.setDataMode(DataMode.SUPABASE_DEMO)
      repository.loginWithSupabase(email.trim(),password) { ok, message ->
        isLoggingIn = false
        if (ok) { password = ""; onLoginSuccess() } else error = message
      }
    }
  }
  Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding(), contentAlignment = Alignment.Center) {
      Column(Modifier.widthIn(max=480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(Spacing.xl20),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl24)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          Icon(Icons.Default.AutoStories,null,tint=MaterialTheme.colorScheme.primary)
          Text("REVENEX",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          Text("Welcome to your school",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
          Text("Sign in to your assigned account. Your school and access are verified securely.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md12)) {
          OutlinedTextField(email,{email=it;error=null},Modifier.fillMaxWidth(),label={Text("Email")},singleLine=true,enabled=!isLoggingIn,
            keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Email,imeAction=androidx.compose.ui.text.input.ImeAction.Next),shape=RoundedCornerShape(Radius.sm))
          OutlinedTextField(password,{password=it;error=null},Modifier.fillMaxWidth(),label={Text("Password")},singleLine=true,enabled=!isLoggingIn,
            visualTransformation=PasswordVisualTransformation(), keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(imeAction=androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions=androidx.compose.foundation.text.KeyboardActions(onDone={signIn()}),shape=RoundedCornerShape(Radius.sm))
          error?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall) }
          Button(onClick=signIn,enabled=!isLoggingIn && email.isNotBlank() && password.isNotBlank(),shape=RoundedCornerShape(Radius.sm),
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("submit_supabase_login")) { Text(if(isLoggingIn) "Signing in…" else "Sign in") }
          TextButton(onClick={help=true},enabled=!isLoggingIn) { Text("Need help signing in?") }
        }
        HorizontalDivider()
        Column {
          Text("Explore with sample school data",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          OutlinedButton(onClick={demoSheet=true;error=null},enabled=!isLoggingIn,modifier=Modifier.fillMaxWidth().testTag("try_demo"),shape=RoundedCornerShape(Radius.sm)) { Text("Try demo") }
        }
        Text("School ERP · Demonstration build",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
  if (demoSheet) ModalBottomSheet(onDismissRequest={if(!isLoggingIn) demoSheet=false}) {
    Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(Spacing.screenPadding)) {
      DemoModeLoginContent(selectedRole,{selectedRole=it;error=null},isLoggingIn,error) {
        isLoggingIn=true
        if(mode!=DataMode.DEMO) repository.setDataMode(DataMode.DEMO)
        repository.switchRole(selectedRole) { isLoggingIn=false;demoSheet=false;onLoginSuccess() }
      }
    }
  }
  if(help) AlertDialog(onDismissRequest={help=false},title={Text("Account help")},text={Text("Use the account assigned by your school. Contact the school administrator for account creation, password recovery or access changes. This demo does not offer self-service password reset.")},confirmButton={TextButton(onClick={help=false}) {Text("Done")}})
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

