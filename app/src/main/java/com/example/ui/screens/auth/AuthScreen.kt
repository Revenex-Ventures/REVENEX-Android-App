package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.datasource.DataMode
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.theme.*

/** A portal is an entry hint, never an authorization decision. */
private enum class Portal(val label: String, val detail: String) {
  PRINCIPAL("Principal", "School leadership"),
  ADMIN("School Administrator", "School operations"),
  TEACHER("Teacher", "Teaching and classwork"),
  FAMILY("Student / Guardian", "Learning and school updates")
}

@Composable
fun AuthScreen(repository: ErpDataRepository, onLoginSuccess: () -> Unit) {
  val mode by repository.dataMode.collectAsState()
  var portalName by rememberSaveable { mutableStateOf<String?>(null) }
  val portal = Portal.entries.firstOrNull { it.name == portalName }
  var demo by rememberSaveable { mutableStateOf(false) }
  var selectedRole by remember { mutableStateOf(UserRole.PRINCIPAL) }
  var busy by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var email by rememberSaveable { mutableStateOf("") }
  // Password intentionally does not survive process/activity recreation.
  var password by remember { mutableStateOf("") }
  var visible by remember { mutableStateOf(false) }
  var help by remember { mutableStateOf(false) }
  val focus = LocalFocusManager.current
  val back: () -> Unit = { if (!busy) { portalName=null;demo=false;password="";error=null } }
  BackHandler(portal != null || demo) { back() }
  val signIn: () -> Unit = {
    if (!busy && email.isNotBlank() && password.isNotBlank()) {
      focus.clearFocus();busy=true;error=null
      if (mode != DataMode.SUPABASE_DEMO) repository.setDataMode(DataMode.SUPABASE_DEMO)
      // No portal, role, school or child identifier is passed to authentication.
      repository.loginWithSupabase(email.trim(), password) { ok, message ->
        busy=false
        if (ok) { password="";onLoginSuccess() } else error=message ?: "Sign-in was not confirmed. Check your connection and account."
      }
    }
  }
  Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
    if (portal == null && !demo) {
      PhotoPortalEntry(onPortal={portalName=it.name},onDemo={demo=true})
    } else {
      Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(),contentAlignment=Alignment.Center) {
        Column(Modifier.widthIn(max=480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(Spacing.xl20),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
          Row(verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=back,enabled=!busy,modifier=Modifier.testTag("entry_back")) { Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back to account entry") }
            Text("REVENEX",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
          }
          if (demo) {
            Text("Explore a sample school",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text("Local demo works offline. These are synthetic personas, separate from school sign-in.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Column { DemoModeLoginContent(selectedRole,{selectedRole=it},busy,error) {
              if (!busy) {
                busy=true
                if(mode!=DataMode.DEMO) repository.setDataMode(DataMode.DEMO)
                repository.switchRole(selectedRole) {busy=false;onLoginSuccess()}
              }
            } }
          } else {
            Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
              Text("${portal!!.label} sign-in",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
              Text("Use your school account. Your verified identity determines your access after sign-in.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
              OutlinedTextField(email,{email=it;error=null},Modifier.fillMaxWidth().testTag("login_email"),label={Text("Email")},singleLine=true,enabled=!busy,
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email,imeAction=ImeAction.Next),shape=RoundedCornerShape(Radius.input))
              OutlinedTextField(password,{password=it;error=null},Modifier.fillMaxWidth().testTag("login_password"),label={Text("Password")},singleLine=true,enabled=!busy,
                visualTransformation=if(visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon={IconButton(onClick={visible=!visible},enabled=!busy) {Icon(if(visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,if(visible) "Hide password" else "Show password")}},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={signIn()}),shape=RoundedCornerShape(Radius.input))
              error?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall) }
              Button(onClick=signIn,enabled=!busy&&email.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("submit_supabase_login"),shape=RoundedCornerShape(Radius.input)) {
                if(busy) {CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp);Spacer(Modifier.width(Spacing.sm8))}
                Text(if(busy) "Signing in…" else "Sign in")
              }
              TextButton(onClick={help=true},enabled=!busy) {Text("Need help signing in?")}
              TextButton(onClick={demo=true;portalName=null;password="";error=null},enabled=!busy,modifier=Modifier.testTag("try_demo")) {Text("Try demo")}
            }
          }
        }
      }
    }
  }
  if(help) AlertDialog(onDismissRequest={help=false},title={Text("Account help")},text={Text("Use the account assigned by your school. Contact your school administrator for account creation, password recovery or access changes. Local demo is available offline through Try demo.")},confirmButton={TextButton(onClick={help=false}){Text("Done")}})
}

@Composable
private fun PhotoPortalEntry(onPortal: (Portal)->Unit,onDemo: ()->Unit) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val photoHeight = maxHeight * .42f
    val wide = maxWidth >= 720.dp
    @Composable fun Photo(modifier: Modifier) {
      Box(modifier) {
        Image(painterResource(R.drawable.school_classroom_chennai),"Illustrative classroom in Chennai, India",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color.Transparent,MaterialTheme.colorScheme.background))))
      }
    }
    @Composable fun Entries(modifier: Modifier) {
      Column(modifier.padding(horizontal=Spacing.xl20).padding(bottom=Spacing.xl24),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Icon(Icons.Default.AutoStories,null,tint=RevenexBrandOrange)
          Text("REVENEX",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
        }
        Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
          Text("Welcome to Revenex",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
          Text("Choose how you want to continue.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Portal.entries.forEach { portal ->
            OutlinedCard(onClick={onPortal(portal)},modifier=Modifier.fillMaxWidth().testTag("portal_${portal.name.lowercase()}"),shape=RoundedCornerShape(Radius.input),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
              Row(Modifier.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
                Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(Radius.sm)) {
                  Icon(when(portal){Portal.PRINCIPAL->Icons.Default.School;Portal.ADMIN->Icons.Default.ManageAccounts;Portal.TEACHER->Icons.Default.MenuBook;Portal.FAMILY->Icons.Default.FamilyRestroom},null,Modifier.padding(Spacing.sm8).size(24.dp),tint=MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(Modifier.weight(1f)) {
                  Text(portal.label,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
                  Text(portal.detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight,null,tint=MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
        TextButton(onClick=onDemo,modifier=Modifier.align(Alignment.CenterHorizontally).testTag("try_demo")) {Text("Try demo")}
      }
    }
    if (wide) Row(Modifier.fillMaxSize().safeDrawingPadding()) {
      Photo(Modifier.weight(1f).fillMaxHeight())
      Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),contentAlignment=Alignment.Center) {Entries(Modifier.widthIn(max=480.dp).padding(top=Spacing.xl24))}
    } else Column(Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState())) {
      Photo(Modifier.fillMaxWidth().height(photoHeight))
      Entries(Modifier.fillMaxWidth())
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

  val demoRoles = listOf(UserRole.ADMIN, UserRole.PRINCIPAL, UserRole.TEACHER, UserRole.STUDENT, UserRole.PARENT)

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
                UserRole.STUDENT -> "Student"
                else -> "Parent / Guardian"
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
                UserRole.STUDENT -> "Synthetic student · Class 10-A"
                else -> "Synthetic guardian · linked child"
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

