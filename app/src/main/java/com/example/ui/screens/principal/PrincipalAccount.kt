package com.example.ui.screens.principal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.datasource.DataMode
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.screens.operations.*
import com.example.ui.theme.*

@Composable private fun AccountRow(title:String,value:String,icon:ImageVector,action:()->Unit) {
 Row(Modifier.fillMaxWidth().clickable(onClick=action).heightIn(min=Spacing.xxxl48).padding(vertical=Spacing.sm8),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  Icon(icon,null);Text(title,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
  if(value.isNotBlank())Text(value,Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Icon(Icons.Outlined.ChevronRight,null)
 };HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))
}
@Composable fun PrincipalAccount(repository:ErpDataRepository,navigate:(String)->Unit) {
 val actor by repository.currentUser.collectAsState();val students by repository.students.collectAsState()
 val mode by repository.dataMode.collectAsState();val locked by repository.isAppLockEnabled.collectAsState()
 val session=if(mode==DataMode.SUPABASE_DEMO)runCatching{repository.supabaseDataSource.sessionId()}.getOrDefault("Not loaded")else students.map{it.sessionId}.distinct().singleOrNull() ?: "Loaded session"
 val year=Regex("(20\\d{2})[_-](20\\d{2})").find(session)?.let{"${it.groupValues[1]}–${it.groupValues[2].takeLast(2)}"} ?: "Loaded session"
 var dialog by rememberSaveable(actor.id){mutableStateOf<String?>(null)}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_account"),contentPadding=PaddingValues(if(LocalPrincipalDark.current)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
  item {PrincipalPageHeader("School & account")}
  item {Row(Modifier.fillMaxWidth().padding(vertical=Spacing.sm8),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
   Avatar(actor.name.replace(Regex("^(Dr\\.?|Prof\\.?|Mr\\.?|Mrs\\.?|Ms\\.?)\\s+"),""),size=PrincipalLayout.listAvatar,background=PrincipalColors.blue,textColor=PrincipalColors.blueInk)
   Column(Modifier.weight(1f)){Text(actor.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("Principal",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }}
  item {Column {
   AccountRow("My profile","",Icons.Outlined.Person){dialog="My profile"}
   AccountRow("School",if(mode==DataMode.DEMO)"Local Demo"else"…${actor.schoolId.takeLast(6)}",Icons.Outlined.School){dialog="School"}
   AccountRow("Academic year",year,Icons.Outlined.CalendarMonth){dialog="Academic year"}
   AccountRow("Staff","Directory & allocations",Icons.Outlined.People){navigate(Screen.Teachers.route)}
   AccountRow("Audit log","Your school activity",Icons.Outlined.History){navigate(Screen.Audit.route)}
  }}
  item {
   val appearance=LocalAppearance.current
   val selector: @Composable () -> Unit = {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("principal_appearance")) {
     Appearance.entries.forEachIndexed { index, option ->
      SegmentedButton(
       selected = appearance?.selection == option,
       onClick = { appearance?.select(option) },
       icon = {}, shape = SegmentedButtonDefaults.itemShape(index, Appearance.entries.size),
       colors = SegmentedButtonDefaults.colors(activeContainerColor = MaterialTheme.colorScheme.primary, activeContentColor = MaterialTheme.colorScheme.onPrimary)
      ) { Text(option.label, style = MaterialTheme.typography.labelMedium) }
     }
    }
   }
   BoxWithConstraints(Modifier.fillMaxWidth()) {
    if(maxWidth<PrincipalLayout.appearanceBreakpoint||LocalDensity.current.fontScale>1.2f)Column(verticalArrangement=Arrangement.spacedBy(Spacing.sm8)){Text("Appearance",style=MaterialTheme.typography.bodyMedium);selector()}
    else Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Icon(Icons.Outlined.LightMode,null);Text("Appearance",style=MaterialTheme.typography.bodyMedium);Box(Modifier.weight(1f)){selector()}}
   }
  }
  item {Column {
   AccountRow("Language","English",Icons.Outlined.Language){dialog="Language"}
  }}
  item {TextButton(onClick={repository.logout();navigate(Screen.Auth.route)},modifier=Modifier.testTag("principal_sign_out"),colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)){Icon(Icons.Outlined.Logout,null);Spacer(Modifier.width(Spacing.md12));Text("Sign out")}}
 }
 dialog?.let {title->AlertDialog(onDismissRequest={dialog=null},title={Text(title)},text={Column(Modifier.heightIn(max=PrincipalLayout.historyHeight).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {when(title){
  "My profile"->{Text(actor.name);SchoolDetailRow("Email",actor.email);SchoolDetailRow("Phone",actor.phone.ifBlank{"Not provided"});SchoolDetailRow("Role",actor.role.displayName)
   TextButton(onClick={dialog=null;navigate(Screen.Notifications.route)}){Text("Notifications")}
   TextButton(onClick={dialog=null;navigate(Screen.StudyMaterial.route)}){Text("Documents")}
   TextButton(onClick={dialog="App lock"}){Text("App lock · ${if(locked)"On"else"Off"}")}
   TextButton(onClick={dialog="Help"}){Text("Help")}
  }
  "School"->{SchoolDetailRow("School ID",actor.schoolId);Text(if(mode==DataMode.DEMO)"Local sample school. No cloud account or payment is used."else"Signed-in school account · Supabase")}
  "Academic year"->{SchoolDetailRow("Session",session);Text("Session changes are not available in this app. Contact the school administrator.")}
  "Language"->Text("The app interface currently supports English. School content is shown in its original language.")
  "App lock"->Row(verticalAlignment=Alignment.CenterVertically){Text("Require device authentication on resume",Modifier.weight(1f));Switch(locked,{repository.setAppLockEnabled(it)})}
  else->Text("Contact your school administrator for access or record corrections. Use the in-app inbox for updates. Background device push is not configured.")
 }}},confirmButton={TextButton(onClick={dialog=null}){Text("Done")}})}
}

@Composable fun PrincipalMore(repository:ErpDataRepository,navigate:(String)->Unit,addStudent:()->Unit,addStaff:()->Unit,notice:()->Unit) {
 val actor by repository.currentUser.collectAsState();var query by rememberSaveable(actor.id){mutableStateOf("")}
 val destinations=WorkspaceDirectory.visible(actor.role,query).filterNot{it.route in setOf(Screen.Students.route,Screen.Teachers.route,Screen.Fees.route,Screen.Attendance.route,Screen.PrincipalAttendance.route)}
 LazyColumn(Modifier.fillMaxSize().testTag("principal_more"),contentPadding=PaddingValues(if(LocalPrincipalDark.current)Spacing.xl20 else Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
  item {PrincipalPageHeader("More");OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Search modules")},leadingIcon={Icon(Icons.Outlined.Search,null)})}
  if(query.isBlank())item {PrincipalHeading("Create") {SchoolRow("Add student","Admission",Icons.Outlined.PersonAdd,addStudent);SchoolRow("Add staff","Faculty record",Icons.Outlined.PersonAdd,addStaff);SchoolRow("Publish notice","School announcement",Icons.Outlined.Campaign,notice)}}
  if(destinations.isEmpty())item {EmptyStateView("No modules","Try a different search.")}
  items(destinations,key={it.route}){d->
   val label=when(d.route){Screen.Leaves.route->"Approvals";Screen.Profile.route->"School & account";Screen.Exams.route->"Exams";Screen.Audit.route->"Audit log";else->d.title}
   AccountRow(label,"",when(d.area){WorkspaceArea.ACADEMICS->Icons.Outlined.MenuBook;WorkspaceArea.REPORTS->Icons.Outlined.BarChart;WorkspaceArea.COMMUNICATION->Icons.Outlined.Notifications;WorkspaceArea.SETTINGS->Icons.Outlined.Settings;else->Icons.Outlined.Apps}){navigate(d.route)}
  }
 }
}
