package com.example.ui.screens.communication

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.navigation.Screen
import kotlinx.coroutines.launch

// ============================================================================
// AUDIT TRAIL & BROADCASTS WORKSPACE
// Segmented sub-tab: "System Audit Logs" ↔ "Parent Broadcasts"
// ============================================================================
@Composable
fun NoticesScreen(repository: ErpDataRepository,onShowCreateNotice: () -> Unit,initialAudit:Boolean=false) {
  val notices by repository.notices.collectAsState()
  val logs by repository.auditLogs.collectAsState()
  val actor by repository.currentUser.collectAsState()
  val administrator=actor.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)
  var audit by remember(actor.id,initialAudit){mutableStateOf(initialAudit && administrator)}
  var filter by remember(actor.id){mutableStateOf("All")}
  var expanded by remember(actor.id){mutableStateOf<String?>(null)}
  val filtered=logs.filter {filter=="All"||it.actionType.equals(filter,true)}
  Column(Modifier.fillMaxSize().testTag("notices_screen")) {
    WorkspacePageHeader(if(audit)"School activity" else "Notices & circulars",Modifier.padding(horizontal=Spacing.screenPadding)) {
      if(administrator) Button(onClick=onShowCreateNotice,shape=RoundedCornerShape(Radius.sm)){Icon(Icons.Default.Add,null);Text("Add")}
    }
    if(administrator) TabRow(selectedTabIndex=if(audit)1 else 0,containerColor=MaterialTheme.colorScheme.background) {
      Tab(selected=!audit,onClick={audit=false},text={Text("Notices")});Tab(selected=audit,onClick={audit=true},text={Text("Audit log")})
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      if(audit) {
        item {LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)){items(listOf("All")+logs.map {it.actionType}.distinct()){value->SchoolFilterChip(filter==value,{filter=value},{Text(value.replace('_',' '))})}}}
        if(filtered.isEmpty()) item {EmptyStateView(title="No activity in this view",message="Confirmed school activity appears here when available.")}
        items(filtered,key={it.id}) {log->SchoolGroup {
          SchoolRow(log.actionSummary,"${log.actorName} · ${log.timestamp}",Icons.Default.History,{expanded=if(expanded==log.id)null else log.id})
          AnimatedVisibility(expanded==log.id){Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            Text(log.details,style=MaterialTheme.typography.bodyMedium);Text("${log.actorRole} · ${log.actionType}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(log.targetScholar.isNotBlank()) Text(log.targetScholar,style=MaterialTheme.typography.bodySmall)
          }}
        }}
      } else {
        if(notices.isEmpty()) item {EmptyStateView(title="No school notices",message="Published announcements appear here when available to your account.")}
        items(notices,key={it.id}) {notice->SchoolGroup {
          SchoolRow(notice.title,"${notice.category.label} · ${notice.publishedDate}",Icons.Default.Campaign,{expanded=if(expanded==notice.id)null else notice.id},if(notice.isImportant)"Important" else null)
          AnimatedVisibility(expanded==notice.id){Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            Text(notice.content,style=MaterialTheme.typography.bodyMedium)
            Text("${notice.authorName} · ${notice.authorRole}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Audience: ${notice.targetAudience}",style=MaterialTheme.typography.bodySmall)
            if(notice.attachmentName.isNotBlank()) Text("Attachment: ${notice.attachmentName}",style=MaterialTheme.typography.bodySmall)
          }}
        }}
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  repository: ErpDataRepository,
  onBack: () -> Unit,
  onNavigateTo: (String) -> Unit = {}
) {
  val notifications by repository.notifications.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  val scope = rememberCoroutineScope()
  var about by remember(currentUser.id) {mutableStateOf(false)}
  if(about) AlertDialog(onDismissRequest={about=false},title={Text("In-app inbox")},text={Text("Updates are stored for your account. Read state is individual. Device push is not configured. Each update opens its related workspace; record-specific links are not available yet.")},confirmButton={TextButton(onClick={about=false}){Text("Done")}})
  var category by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf<String?>(null) }
  var unreadOnly by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf(false) }
  var markingAll by remember(currentUser.id) { mutableStateOf(false) }
  val filtered = notifications.filter { (category == null || it.category.name == category) && (!unreadOnly || !it.isRead) }
  var opening by remember(currentUser.id) { mutableStateOf<String?>(null) }
  var inboxError by remember(currentUser.id) { mutableStateOf<String?>(null) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = { Text("Inbox", style=MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          TextButton(enabled = !markingAll && opening == null && notifications.any { !it.isRead }, onClick = {
            markingAll = true; inboxError = null
            scope.launch {
              try { repository.readAllNotificationsConfirmed() }
              catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                inboxError = "Read state could not be confirmed. Refresh before retrying."
              } finally { markingAll = false }
            }
          }) { Text(if (markingAll) "Saving…" else "Mark all read") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
      verticalArrangement = Arrangement.spacedBy(Spacing.s2)
    ) {
      item {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
          Text("${notifications.count { !it.isRead }} unread",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          IconButton(onClick={about=true}){Icon(Icons.Default.Info,"Inbox information",Modifier.size(Spacing.xl20))}
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
          item { SchoolFilterChip(category == null, { category = null }, { Text("All") }) }
          items(notifications.map { it.category }.distinct()) { value ->
            SchoolFilterChip(category == value.name, { category = value.name }, { Text(value.label) })
          }
          item { SchoolFilterChip(unreadOnly, { unreadOnly = !unreadOnly }, { Text("Unread") }) }
        }
      }
      inboxError?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
      if (filtered.isEmpty()) {
        item { EmptyStateView(title = "No matching updates", message = "School updates appear here. Clear filters to see your full inbox.") }
      }
      filtered.groupBy { notice ->
        notice.timestamp.take(10).takeIf {it.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))} ?: "Recent updates"
      }.forEach { (date,entries) ->
        item { Text(date,Modifier.padding(top=Spacing.md12,bottom=Spacing.xs4),style=MaterialTheme.typography.labelLarge,fontWeight=FontWeight.Bold) }
        items(entries,key={it.id}) { notif ->
          Surface(onClick={
          opening = notif.id; inboxError = null
          scope.launch {
            try {
              if (!notif.isRead) repository.readNotificationConfirmed(notif.id)
              onNavigateTo(notificationDestination(notif.category))
            } catch (e: Exception) {
              if (e is kotlinx.coroutines.CancellationException) throw e
              inboxError = "Could not open this update. Read state is not confirmed; tap again to retry."
            } finally { opening = null }
          }
          },enabled=opening==null&&!markingAll,color=MaterialTheme.colorScheme.background) {
            Row(Modifier.fillMaxWidth().semantics {stateDescription=if(notif.isRead)"Read" else "Unread"}.padding(vertical=Spacing.sm8),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
              val (well,ink)=when(notif.category) {
                NotificationCategory.FEES->SemanticColors.butterContainer to SemanticColors.onButter
                NotificationCategory.ATTENDANCE->SemanticColors.successContainer to SemanticColors.onSuccess
                NotificationCategory.HOMEWORK,NotificationCategory.EXAMS->SemanticColors.lavenderContainer to SemanticColors.onLavender
                else->SemanticColors.peachContainer to SemanticColors.onPeach
              }
              Surface(shape=CircleShape,color=well) {Icon(when(notif.category){NotificationCategory.FEES->Icons.Default.CurrencyRupee;NotificationCategory.ATTENDANCE->Icons.Default.HowToReg;NotificationCategory.HOMEWORK->Icons.Default.Description;else->Icons.Default.Campaign},null,Modifier.padding(Spacing.sm8).size(18.dp),tint=ink)}
              Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
                  Text(notif.title,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=if(notif.isRead)FontWeight.Medium else FontWeight.Bold)
                  Text(if(opening==notif.id)"Opening…" else shortSchoolTimestamp(notif.timestamp),Modifier.semantics{contentDescription=notif.timestamp},style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                  if(!notif.isRead) Box(Modifier.padding(top=Spacing.xs4).size(6.dp).background(MaterialTheme.colorScheme.primary,CircleShape))
                }
                Text(notif.message,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)

              }
            }
          }
          HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
        }
      }

    }
  }
}

internal fun notificationDestination(category: NotificationCategory): String = when (category) {
  NotificationCategory.ATTENDANCE -> Screen.Attendance.route
  NotificationCategory.FEES -> Screen.Fees.route
  NotificationCategory.HOMEWORK -> Screen.Homework.route
  NotificationCategory.EXAMS -> Screen.ReportCard.route
  NotificationCategory.LEAVE -> Screen.Leaves.route
  NotificationCategory.ANNOUNCEMENTS -> Screen.Notices.route
  else -> Screen.OperationsHub.route
}
