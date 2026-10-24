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
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(repository: ErpDataRepository, onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
  val user by repository.currentUser.collectAsState()
  val locked by repository.isAppLockEnabled.collectAsState()
  val cloud = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO
  val session = if(cloud && user.id.isNotBlank()) runCatching {repository.supabaseDataSource.sessionId()}.getOrDefault("Not loaded") else "Local sample session"
  var details by remember { mutableStateOf(false) }
  var help by remember { mutableStateOf(false) }
  var security by remember(user.id) {mutableStateOf(false)}
  Scaffold(topBar={TopAppBar(title={Text("Settings",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}})}) { padding ->
    LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("profile_screen_list"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
      item { SchoolGroup {
        Row(Modifier.fillMaxWidth().clickable{details=true}.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
          Avatar(user.name,size=Spacing.xxl32)
          Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(Spacing.xs4)) {
            Text(user.name,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
            Text(user.role.displayName,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if(cloud)"School account · Supabase" else "Local Demo",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
          }
          Icon(Icons.Default.ChevronRight,null)
        }
      } }
      item { SchoolGroup {
        SchoolRow("Notifications","Your school inbox",Icons.Default.Notifications,{onNavigateTo(com.example.ui.navigation.Screen.Notifications.route)})
        HorizontalDivider()
        SchoolRow("Documents","Notes and shared resources",Icons.Default.Folder,{onNavigateTo(com.example.ui.navigation.Screen.StudyMaterial.route)})
        HorizontalDivider()
        SchoolRow("Help","School access and support",Icons.Default.HelpOutline,{help=true})
      } }
      item { SchoolSection("Appearance") {
        val appearance=LocalAppearance.current
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
          Appearance.entries.forEachIndexed { index,option ->
            SegmentedButton(selected=appearance?.selection==option,onClick={appearance?.select(option)},shape=SegmentedButtonDefaults.itemShape(index,Appearance.entries.size),
              colors=SegmentedButtonDefaults.colors(activeContainerColor=MaterialTheme.colorScheme.primary,activeContentColor=MaterialTheme.colorScheme.onPrimary)) {Text(option.label)}
          }
        }
      } }
      item {SchoolGroup {
        SchoolRow("App lock",if(locked)"On" else "Off",Icons.Default.Fingerprint,{security=true})
        HorizontalDivider()
        SchoolRow("School",if(cloud)"ID …${user.schoolId.takeLast(6)} · Session …${session.takeLast(6)}" else "Revenex Public School · sample",Icons.Default.School,{details=true})
      }}
      item { OutlinedButton(onClick={repository.logout();onNavigateTo(com.example.ui.navigation.Screen.Auth.route)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(Radius.sm),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.error)) {
        Icon(Icons.Default.Logout,null);Spacer(Modifier.width(Spacing.sm8));Text("Sign out")
      } }
    }
  }
  if(security) AlertDialog(onDismissRequest={security=false},title={Text("App lock")},text={Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    Text("Require fingerprint or PIN when resuming",Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
    Switch(checked=locked,onCheckedChange={repository.setAppLockEnabled(it)})
  }},confirmButton={TextButton(onClick={security=false}){Text("Done")}})
  if(details) AlertDialog(onDismissRequest={details=false},title={Text(user.name)},text={Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
    Text(user.role.displayName);Text(user.email);Text("Phone: ${user.phone.ifBlank { "Not provided" }}");Text("School: ${user.schoolId}");Text("Session: $session")
  }},confirmButton={TextButton(onClick={details=false}) {Text("Done")}})
  if(help) AlertDialog(onDismissRequest={help=false},title={Text("School support")},text={Text("Contact your school administrator for account access, school records and password recovery. Your signed-in account determines which school information is available. Device push notifications are not configured in this demo; use the in-app inbox.")},confirmButton={TextButton(onClick={help=false}) {Text("Done")}})
}
