package com.example.ui.screens.operations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.LeaveStatus
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
import com.example.ui.components.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationsHubScreen(repository: ErpDataRepository,onNavigateTo: (String)->Unit,onShowAddStudent:()->Unit,onShowAddTeacher:()->Unit,onShowCreateNotice:()->Unit,onShowApplyLeave:()->Unit,initialArea:WorkspaceArea?=null) {
  val actor by repository.currentUser.collectAsState()
  var query by androidx.compose.runtime.saveable.rememberSaveable(actor.id){mutableStateOf("")}
  var area by androidx.compose.runtime.saveable.rememberSaveable(actor.id,initialArea){mutableStateOf(initialArea?.name)}
  var boundaries by remember {mutableStateOf(false)}
  val administrator=actor.role in setOf(UserRole.ADMIN,UserRole.PRINCIPAL)
  val destinations=WorkspaceDirectory.visible(actor.role,query,WorkspaceArea.entries.find {it.name==area})
  val available=WorkspaceDirectory.visible(actor.role).map {it.area}.distinct()
  val columns=if(androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp>=840)2 else 1
  LazyColumn(Modifier.fillMaxSize().testTag("operations_hub_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.xl24)) {
    item {WorkspacePageHeader(if(initialArea==WorkspaceArea.ACADEMICS)"Academics" else "Modules")
      Text("Tools for your account",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    item {
      OutlinedTextField(query,{query=it},Modifier.fillMaxWidth().testTag("module_search"),placeholder={Text("Search modules")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(query.isNotBlank())IconButton(onClick={query=""}){Icon(Icons.Default.Close,"Clear search")}},singleLine=true,shape=RoundedCornerShape(Radius.sm))
      androidx.compose.foundation.lazy.LazyRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        item {SchoolFilterChip(area==null,{area=null},{Text("All")})}
        items(available){value->SchoolFilterChip(area==value.name,{area=value.name},{Text(value.label)})}
      }
      if(query.isBlank()&&area==null) FlowRow(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
        if(administrator) {
          OutlinedButton(onClick=onShowAddStudent){Text("Add student")};OutlinedButton(onClick=onShowAddTeacher){Text("Add faculty")};OutlinedButton(onClick=onShowCreateNotice){Text("Publish notice")}
        } else if(actor.role!=UserRole.STUDENT) OutlinedButton(onClick=onShowApplyLeave){Text("Apply for leave")}
      }
    }
    if(destinations.isEmpty()) item {EmptyStateView(title="No matching modules",message="Try another search or category.")}
    items(destinations.groupBy {it.area}.toList().chunked(columns)) {groups->
      Row(horizontalArrangement=Arrangement.spacedBy(Spacing.lg16)) {
        groups.forEach {(group,modules)->Column(Modifier.weight(1f)) {SchoolSection(group.label){SchoolGroup {
          modules.forEach {destination->
            val icon=when(group){WorkspaceArea.PEOPLE->Icons.Default.Groups;WorkspaceArea.ACADEMICS->Icons.Default.MenuBook;WorkspaceArea.ATTENDANCE->Icons.Default.FactCheck;WorkspaceArea.FINANCE->Icons.Default.ReceiptLong;WorkspaceArea.OPERATIONS->Icons.Default.Apps;WorkspaceArea.COMMUNICATION->Icons.Default.Notifications;WorkspaceArea.REPORTS->Icons.Default.BarChart;WorkspaceArea.SETTINGS->Icons.Default.Settings}
            SchoolRow(destination.title,destination.description,icon,{onNavigateTo(destination.route)},modifier=Modifier.testTag("workspace_${destination.route.substringBefore('?')}"))
            if(destination!=modules.last()) HorizontalDivider()
          }
        }}}}
        if(groups.size<columns) Spacer(Modifier.weight(1f))
      }
    }
    if(administrator) item {
      TextButton(onClick={boundaries=!boundaries}){Text(if(boundaries)"Hide details" else "Integration details")}
      if(boundaries) Text("Machine/RFID attendance, Tally, payroll, SMS/email and website tools need separate integrations. Hostel, counselling, scholarship and question-bank workflows are not implemented. These are not active modules; no device sync, payroll run or message dispatch happens here.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun LeaveManagementScreen(
  repository: ErpDataRepository,
  onShowApplyLeave: () -> Unit
) {
  val leaveRequests by repository.leaveRequests.collectAsState()
  val currentUser by repository.currentUser.collectAsState()
  var selectedTab by remember { mutableStateOf(0) }
  val scope = rememberCoroutineScope()
  var deciding by remember(currentUser.id) { mutableStateOf<String?>(null) }
  var decisionError by remember(currentUser.id) { mutableStateOf<String?>(null) }
  var confirmDecision by remember(currentUser.id) { mutableStateOf<Pair<String, LeaveStatus>?>(null) }
  var remark by remember { mutableStateOf("") }
  confirmDecision?.let { (id, status) ->
    AlertDialog(onDismissRequest = { confirmDecision = null },
      title = { Text(if (status == LeaveStatus.APPROVED) "Approve leave?" else "Reject leave?") },
      text = { OutlinedTextField(remark, { remark = it }, label = { Text("Remark (optional)") }, supportingText = { Text("The applicant receives an in-app update after confirmation.") }) },
      confirmButton = { TextButton(enabled = remark.length <= 1000, onClick = {
        confirmDecision = null; deciding = id; decisionError = null
        scope.launch {
          try { repository.decideLeaveConfirmed(id,status,remark) }
          catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            decisionError = "Decision not confirmed. Reload the request and check access or school setup before retrying."
          } finally { deciding = null }
        }
      }) { Text("Confirm") } },
      dismissButton = { TextButton(onClick = { confirmDecision = null }) { Text("Cancel") } })
  }

  val pending=leaveRequests.filter {it.status==LeaveStatus.PENDING}
  val history=leaveRequests.filter {it.status!=LeaveStatus.PENDING}
  val visible=if(selectedTab==0)pending else history
  var expanded by remember(currentUser.id){mutableStateOf<String?>(null)}
  Column(Modifier.fillMaxSize().testTag("leave_management_screen")) {
    WorkspacePageHeader("Leave requests",Modifier.padding(horizontal=Spacing.screenPadding)) {
      Button(onClick=onShowApplyLeave,modifier=Modifier.testTag("fab_apply_leave"),shape=RoundedCornerShape(Radius.sm)){Icon(Icons.Default.Add,null);Text("Apply")}
    }
    TabRow(selectedTabIndex=selectedTab,containerColor=MaterialTheme.colorScheme.background) {
      Tab(selected=selectedTab==0,onClick={selectedTab=0},text={Text("Pending (${pending.size})")})
      Tab(selected=selectedTab==1,onClick={selectedTab=1},text={Text("History (${history.size})")})
    }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      decisionError?.let {message->item {Text(message,color=MaterialTheme.colorScheme.error)}}
      if(visible.isEmpty()) item {EmptyStateView(title="No requests in this view",message="Leave applications and confirmed decisions appear here.")}
      items(visible,key={it.id}) {leave->SchoolGroup {
        SchoolRow(leave.applicantName,"${leave.leaveType} · ${leave.startDate} – ${leave.endDate}",Icons.Default.EventBusy,{expanded=if(expanded==leave.id)null else leave.id},leave.status.label)
        AnimatedVisibility(expanded==leave.id) {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          Text("${leave.applicantRole.displayName} · ${leave.classOrDept} · ${leave.daysCount} days",style=MaterialTheme.typography.bodySmall)
          Text(leave.reason,style=MaterialTheme.typography.bodyMedium)
          Text("Applied ${leave.appliedDate}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
          if(leave.approverRemark.isNotBlank()) Text("School remark: ${leave.approverRemark}",style=MaterialTheme.typography.bodySmall)
          if(leave.status==LeaveStatus.PENDING&&currentUser.role in setOf(UserRole.PRINCIPAL,UserRole.ADMIN)) Row(horizontalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
            OutlinedButton(enabled=deciding==null,onClick={remark="";confirmDecision=leave.id to LeaveStatus.REJECTED}){Text("Reject")}
            Button(enabled=deciding==null,onClick={remark="";confirmDecision=leave.id to LeaveStatus.APPROVED}){Text(if(deciding==leave.id)"Saving…" else "Approve")}
          }
        }}
      }}
    }
  }
}

@Composable
fun TransportScreen(repository: ErpDataRepository) {
  val routes by repository.transportRoutes.collectAsState()
  var query by remember {mutableStateOf("")}
  val visible=routes.filter {query.isBlank()||it.routeName.contains(query,true)||it.routeNumber.contains(query,true)||it.stops.any {stop->stop.contains(query,true)}}
  LazyColumn(Modifier.fillMaxSize().testTag("transport_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("School transport")
      OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search route or stop")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.sm))}
    if(visible.isEmpty()) item {EmptyStateView(title="No matching routes",message="Routes appear when configured and available to your school account.")}
    items(visible,key={it.id}) {route->var expanded by remember(route.id){mutableStateOf(false)}
      SchoolGroup {
        SchoolRow(route.routeName,"${route.routeNumber} · ${route.vehicleNumber}",Icons.Default.DirectionsBus,{expanded=!expanded},"${route.stops.size} stops")
        AnimatedVisibility(expanded) {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
          SchoolDetailRow("Driver",route.driverName)
          SchoolDetailRow("Contact",route.driverPhone)
          Text("Stops & timings",style=MaterialTheme.typography.titleSmall)
          route.stops.forEachIndexed {index,stop->Row(horizontalArrangement=Arrangement.spacedBy(Spacing.md12)){Text((index+1).toString(),style=MaterialTheme.typography.labelLarge);Text(stop,style=MaterialTheme.typography.bodySmall)}}
        }}
      }
    }
  }
}

@Composable
fun LibraryScreen(repository: ErpDataRepository) {
  val books by repository.libraryBooks.collectAsState()
  val cloud=repository.dataMode.collectAsState().value==com.example.data.datasource.DataMode.SUPABASE_DEMO
  var query by remember {mutableStateOf("")}
  val visible=books.filter {query.isBlank()||it.title.contains(query,true)||it.author.contains(query,true)||it.shelfLocation.contains(query,true)}
  LazyColumn(Modifier.fillMaxSize().testTag("library_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("School library")
      Text(if(cloud)"Catalogue only. Cloud borrower tracking and circulation are not configured." else "Local sample catalogue and circulation preview.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search title, author or shelf")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.sm))}
    if(visible.isEmpty()) item {EmptyStateView(title="No matching books",message="No accessible catalogue records match this search.")}
    items(visible,key={it.id}) {book->SchoolGroup {Row(Modifier.padding(Spacing.md12),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Spacing.md12)) {
      Icon(Icons.Default.MenuBook,null,tint=MaterialTheme.colorScheme.primary)
      Column(Modifier.weight(1f)){Text(book.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);Text(book.author,style=MaterialTheme.typography.bodySmall);Text("${book.shelfLocation} · ${book.availableCopies}/${book.totalCopies} available",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
      if(!cloud) TextButton(onClick={if(book.availableCopies>0)repository.issueBook(book.id)else repository.returnBook(book.id)}){Text(if(book.availableCopies>0)"Issue" else "Return")}
    }}}
  }
}

@Composable
fun InventoryScreen(repository: ErpDataRepository) {
  val inventory by repository.inventory.collectAsState()
  var query by remember {mutableStateOf("")}
  val visible=inventory.filter {query.isBlank()||it.itemName.contains(query,true)||it.category.contains(query,true)||it.location.contains(query,true)}
  LazyColumn(Modifier.fillMaxSize().testTag("inventory_screen"),contentPadding=PaddingValues(Spacing.screenPadding),verticalArrangement=Arrangement.spacedBy(Spacing.md12)) {
    item {WorkspacePageHeader("School inventory")
      OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Search asset, category or location")},singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(Radius.sm))}
    if(visible.isEmpty()) item {EmptyStateView(title="No matching assets",message="No accessible inventory records match this search.")}
    items(visible,key={it.id}) {asset->SchoolGroup {Column(Modifier.padding(Spacing.md12),verticalArrangement=Arrangement.spacedBy(Spacing.sm8)) {
      Row(Modifier.fillMaxWidth()){Text(asset.itemName,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold);StatusBadge(status=asset.condition)}
      Text("${asset.category} · ${asset.location}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
      SchoolDetailRow("Quantity", "${asset.quantity} units")
      SchoolDetailRow("Estimated value", java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(asset.estimatedValue))
    }}}
  }
}
