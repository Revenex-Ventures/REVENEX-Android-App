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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.selection.selectable
import com.example.ui.components.RevenexBrand
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

@OptIn(ExperimentalMaterial3Api::class)
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
      Scaffold(modifier=Modifier.fillMaxSize().safeDrawingPadding().imePadding(),containerColor=MaterialTheme.colorScheme.background,
        topBar={Row(Modifier.fillMaxWidth().padding(horizontal=Spacing.sm8),verticalAlignment=Alignment.CenterVertically) {
          IconButton(onClick=back,enabled=!busy,modifier=Modifier.testTag("entry_back")){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back to account entry")}
          if(demo) RevenexBrand() else Text("School account",style=MaterialTheme.typography.titleSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }},bottomBar={
          Surface(color=MaterialTheme.colorScheme.background) {
            Column(Modifier.widthIn(max=EntryTokens.formWidth).fillMaxWidth().padding(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
              if(demo) Button(onClick={
                if(!busy){busy=true;if(mode!=DataMode.DEMO)repository.setDataMode(DataMode.DEMO)
                  repository.switchRole(selectedRole){busy=false;onLoginSuccess()}}
              },enabled=!busy,modifier=Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48).testTag("submit_login_button"),shape=RoundedCornerShape(Radius.input)) {
                Text(if(busy)"Opening school…" else "Enter ${selectedRole.displayName} Dashboard")
              } else Button(onClick=signIn,enabled=!busy&&email.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().heightIn(min=Spacing.xxxl48).testTag("submit_supabase_login"),shape=RoundedCornerShape(Radius.input)) {
                if(busy){CircularProgressIndicator(Modifier.size(Spacing.xl20),strokeWidth=2.dp);Spacer(Modifier.width(Spacing.sm8))}
                Text(if(busy)"Signing in…" else "Sign in")
              }
            }
          }
        }) { padding ->
          Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.TopCenter) {
            Column(Modifier.widthIn(max=EntryTokens.formWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(Spacing.xl20),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
              if(demo) {
                Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                  Text("Explore a school day",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                  Text("Choose a sample persona. Local Demo works offline and keeps school sign-in separate.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DemoRolePicker(selectedRole,{selectedRole=it},busy)
                Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(Radius.md)) {
                  Row(Modifier.padding(Spacing.lg16),horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
                    Icon(Icons.Default.Info,null,tint=MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Sample school data · no real student accounts or payments",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSecondaryContainer)
                  }
                }
              } else {
                RevenexBrand(Modifier.align(Alignment.CenterHorizontally).padding(top=Spacing.sm8),stacked=true)
                Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                  Text("${portal!!.label} sign-in",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                  Text("Continue with your assigned school account.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                    Icon(Icons.Default.Lock,null,Modifier.size(Spacing.lg16),tint=MaterialTheme.colorScheme.primary)
                    Text("School sign-in · Supabase",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary,modifier=Modifier.testTag("supabase_entry_label"))
                  }
                }
                Column(verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
                  val fieldColors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.surfaceVariant,unfocusedContainerColor=MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent,disabledIndicatorColor=Color.Transparent)
                  TextField(email,{email=it;error=null},Modifier.fillMaxWidth().testTag("login_email"),label={Text("Email")},singleLine=true,enabled=!busy,leadingIcon={Icon(Icons.Default.AlternateEmail,null)},
                    trailingIcon={if(email.isNotBlank())IconButton(onClick={email=""},enabled=!busy){Icon(Icons.Default.Close,"Clear email")}},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email,imeAction=ImeAction.Next),shape=RoundedCornerShape(Radius.input),colors=fieldColors)
                  TextField(password,{password=it;error=null},Modifier.fillMaxWidth().testTag("login_password"),label={Text("Password")},singleLine=true,enabled=!busy,leadingIcon={Icon(Icons.Default.LockOutline,null)},
                    visualTransformation=if(visible)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={IconButton(onClick={visible=!visible},enabled=!busy){Icon(if(visible)Icons.Default.VisibilityOff else Icons.Default.Visibility,if(visible)"Hide password" else "Show password")}},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={signIn()}),shape=RoundedCornerShape(Radius.input),colors=fieldColors)
                  error?.let {message->Surface(color=MaterialTheme.colorScheme.errorContainer,shape=RoundedCornerShape(Radius.sm)){Text(message,Modifier.padding(Spacing.md12),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onErrorContainer)}}
                  TextButton(onClick={help=true},enabled=!busy){Text("Need help signing in?")}
                }
                HorizontalDivider()
                Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                  Text("Just exploring?",style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
                  OutlinedButton(onClick={demo=true;portalName=null;password="";error=null},enabled=!busy,modifier=Modifier.fillMaxWidth().testTag("try_demo"),shape=RoundedCornerShape(Radius.input)){Text("Try demo")}
                  Text("Your school sets your role and access after sign-in.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
    val photoHeight = maxHeight * EntryTokens.photoFraction
    val wide = maxWidth >= EntryTokens.tabletBreakpoint
    @Composable fun Photo(modifier: Modifier) {
      Box(modifier) {
        Image(painterResource(R.drawable.school_classroom_chennai),"Illustrative classroom in Chennai, India",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color.Transparent,MaterialTheme.colorScheme.background))))
      }
    }
    @Composable fun Entries(modifier: Modifier) {
      Column(modifier.padding(horizontal=Spacing.xl20).padding(bottom=Spacing.xl24),verticalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          RevenexBrand()
        }
        Column(verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
          Text("Welcome to Revenex",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
          Text("Choose your portal for school sign-in with Supabase.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
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


@Composable
private fun DemoRolePicker(selectedRole: UserRole,onSelected: (UserRole)->Unit,busy: Boolean) {
  val roles=listOf(UserRole.ADMIN,UserRole.PRINCIPAL,UserRole.TEACHER,UserRole.STUDENT,UserRole.PARENT)
  val large=LocalDensity.current.fontScale>1.25f
  fun title(role: UserRole)=when(role){UserRole.ADMIN->"School Administrator";UserRole.PRINCIPAL->"Principal / Academic Leader";UserRole.TEACHER->"Teacher / Faculty";UserRole.STUDENT->"Student";else->"Parent / Guardian"}
  fun detail(role: UserRole)=when(role){UserRole.ADMIN->"Admissions, people & operations";UserRole.PRINCIPAL->"School overview & decisions";UserRole.TEACHER->"Classes, tasks & results";UserRole.STUDENT->"Lessons, assignments & fees";else->"Child updates & school accounts"}
  @Composable fun Persona(role: UserRole,modifier: Modifier=Modifier) {
    val chosen=selectedRole==role
    Surface(modifier.fillMaxWidth().selectable(chosen,onClick={onSelected(role)},enabled=!busy,role=Role.RadioButton).testTag("role_select_${role.name.lowercase()}"),
      shape=RoundedCornerShape(Radius.lg),color=if(chosen)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
      border=androidx.compose.foundation.BorderStroke(if(chosen)2.dp else 1.dp,if(chosen)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
      Column(Modifier.padding(Spacing.lg16),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
          Icon(when(role){UserRole.ADMIN->Icons.Default.ManageAccounts;UserRole.PRINCIPAL->Icons.Default.School;UserRole.TEACHER->Icons.Default.MenuBook;UserRole.STUDENT->Icons.Default.Person;else->Icons.Default.FamilyRestroom},null,tint=MaterialTheme.colorScheme.primary)
          if(chosen)Icon(Icons.Default.CheckCircle,"Selected",tint=MaterialTheme.colorScheme.primary)
        }
        Text(title(role),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
        Text(detail(role),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
  BoxWithConstraints(Modifier.fillMaxWidth()) {
    Column(verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      if(maxWidth<360.dp||large) roles.forEach{Persona(it)}
      else roles.chunked(2).forEach { pair->
        Row(horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
          pair.forEach {Persona(it,Modifier.weight(1f))}
          if(pair.size==1)Spacer(Modifier.weight(1f))
        }
      }
    }
  }
}
