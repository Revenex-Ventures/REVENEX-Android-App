package com.example.ui.screens.operations

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
import com.example.ui.components.SchoolFilterChip
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun OperationsHubScreen(
  repository: ErpDataRepository,
  onNavigateTo: (String) -> Unit,
  onShowAddStudent: () -> Unit,
  onShowAddTeacher: () -> Unit,
  onShowCreateNotice: () -> Unit,
  onShowApplyLeave: () -> Unit
) {
  val currentUser by repository.currentUser.collectAsState()
  var query by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf("") }
  var selectedArea by androidx.compose.runtime.saveable.rememberSaveable(currentUser.id) { mutableStateOf<String?>(null) }
  var showBoundaries by remember(currentUser.id) { mutableStateOf(false) }
  val modules = WorkspaceDirectory.visible(currentUser.role, query, WorkspaceArea.entries.find { it.name == selectedArea })
  val availableAreas = WorkspaceDirectory.visible(currentUser.role).map { it.area }.distinct()
  val administrator = currentUser.role in setOf(UserRole.ADMIN, UserRole.PRINCIPAL)
  val width = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
  val columns = if (width >= 840) 3 else if (width >= 600) 2 else 1

  LazyColumn(
    Modifier.fillMaxSize().testTag("operations_hub_screen"),
    contentPadding = PaddingValues(Spacing.screenPadding),
    verticalArrangement = Arrangement.spacedBy(Spacing.md12)
  ) {
    item {
      Text("School workspaces", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text("${currentUser.role.displayName} · ${WorkspaceDirectory.visible(currentUser.role).size} available tools",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    item {
      OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("module_search"),
        label = { Text("Search workspaces") }, singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Clear search") } },
        shape = RoundedCornerShape(Radius.input))
    }
    item {
      androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        item { SchoolFilterChip(selectedArea == null, { selectedArea = null }, { Text("All") }) }
        items(availableAreas) { area -> SchoolFilterChip(selectedArea == area.name, { selectedArea = area.name }, { Text(area.label) }) }
      }
    }
    if (query.isBlank() && selectedArea == null) item {
      androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm8)) {
        if (administrator) {
          item { FilledTonalButton(onClick = onShowAddStudent) { Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(Spacing.sm8)); Text("Enrol student") } }
          item { OutlinedButton(onClick = onShowAddTeacher) { Text("Add faculty") } }
          item { OutlinedButton(onClick = onShowCreateNotice) { Text("Publish notice") } }
        } else if (currentUser.role != UserRole.STUDENT) {
          item { FilledTonalButton(onClick = onShowApplyLeave) { Text("Apply for leave") } }
        }
      }
    }
    if (modules.isEmpty()) item {
      EmptyStateView(icon = Icons.Default.SearchOff, title = "No matching workspace", description = "Try a module name or choose another area.")
    }
    modules.groupBy { it.area }.forEach { (area, destinations) ->
      item(key = area.name) {
        Text(area.label, Modifier.padding(top = Spacing.md12), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      }
      items(destinations.chunked(columns)) { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
          row.forEach { destination ->
            val (tint, container) = when (area) {
              WorkspaceArea.PEOPLE, WorkspaceArea.FINANCE -> SemanticColors.onPeach to SemanticColors.peachContainer
              WorkspaceArea.ACADEMICS -> SemanticColors.onLavender to SemanticColors.lavenderContainer
              else -> MaterialTheme.colorScheme.onSecondaryContainer to MaterialTheme.colorScheme.secondaryContainer
            }
            val icon = when (area) {
              WorkspaceArea.PEOPLE -> Icons.Default.Groups
              WorkspaceArea.ACADEMICS -> Icons.Default.MenuBook
              WorkspaceArea.ATTENDANCE -> Icons.Default.FactCheck
              WorkspaceArea.FINANCE -> Icons.Default.AccountBalanceWallet
              WorkspaceArea.OPERATIONS -> Icons.Default.Dashboard
              WorkspaceArea.ACCOUNT -> Icons.Default.NotificationsNone
            }
            OutlinedCard(onClick = { onNavigateTo(destination.route) },
              modifier = Modifier.weight(1f).testTag("workspace_${destination.route.substringBefore('?')}"),
              shape = RoundedCornerShape(Radius.md),
              colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
              Row(Modifier.fillMaxWidth().padding(Spacing.md12), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md12)) {
                Box(Modifier.size(40.dp).background(container, RoundedCornerShape(Radius.sm)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs4)) {
                  Text(destination.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                  Text(destination.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
          repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
      }
    }
    if (administrator) item {
      TextButton(onClick = { showBoundaries = !showBoundaries }, modifier = Modifier.fillMaxWidth()) {
        Text(if (showBoundaries) "Hide integration boundaries" else "Specialist tools & integration boundaries")
        Icon(if (showBoundaries) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
      }
      if (showBoundaries) Column(verticalArrangement = Arrangement.spacedBy(Spacing.md12)) {
        Text("These are not active modules", style = MaterialTheme.typography.titleSmall)
        Text("Machine/RFID attendance needs a device provider and secure event ingestion. Tally, payroll, SMS/email and website tools need separate integrations. Hostel, counselling, scholarship and question-bank workflows are not implemented.", style = MaterialTheme.typography.bodyMedium)
        Text("No device sync, payroll run or message dispatch happens from this directory. See the admin module matrix for scope and setup dependencies.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
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

  val pendingLeaves = leaveRequests.filter { it.status == LeaveStatus.PENDING }
  val historyLeaves = leaveRequests.filter { it.status != LeaveStatus.PENDING }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onShowApplyLeave,
        icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
        text = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_apply_leave")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("leave_management_screen"),
      contentPadding = PaddingValues(bottom = Spacing.xl24)
    ) {
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          modifier = Modifier.fillMaxWidth()
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Pending (${pendingLeaves.size})", fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Processed History (${historyLeaves.size})", fontWeight = FontWeight.Bold) }
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      decisionError?.let { message -> item { Text(message, Modifier.padding(Spacing.screenPadding), color = MaterialTheme.colorScheme.error) } }
      val displayList = if (selectedTab == 0) pendingLeaves else historyLeaves

      if (displayList.isEmpty()) {
        item {
          EmptyStateView(
            icon = Icons.Default.EventAvailable,
            title = if (selectedTab == 0) "No Pending Requests" else "No Processed History",
            description = if (selectedTab == 0) "All student and staff leaves have been processed." else "No historical leave applications recorded."
          )
        }
      } else {
        items(displayList, key = { it.id }) { leave ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = leave.applicantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${leave.classOrDept} • ${leave.applicantRole.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                StatusBadge(status = leave.status.label, type = "leave")
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "${leave.leaveType}: ${leave.startDate} to ${leave.endDate} (${leave.daysCount} Day(s))",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "\"${leave.reason}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              if (leave.approverRemark.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Remark: ${leave.approverRemark}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }

              if (leave.status == LeaveStatus.PENDING && (currentUser.role == UserRole.PRINCIPAL || currentUser.role == UserRole.ADMIN)) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  OutlinedButton(
                    enabled = deciding == null,
                    onClick = { remark = ""; confirmDecision = leave.id to LeaveStatus.REJECTED },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                  ) {
                    Text("Reject")
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    enabled = deciding == null,
                    onClick = { remark = ""; confirmDecision = leave.id to LeaveStatus.APPROVED },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                  ) {
                    Text(if (deciding == leave.id) "Saving…" else "Approve")
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TransportScreen(
  repository: ErpDataRepository
) {
  val routes by repository.transportRoutes.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("transport_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "School Transport",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${routes.size} routes • Scheduled stops and driver contacts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StatusSuccess
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ROUTES", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    if (routes.isEmpty()) item { EmptyStateView("No accessible transport routes", "Route records appear here when configured and available to your school account.") }
    items(routes, key = { it.id }) { route ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = route.routeNumber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Text(
              text = "Bus: ${route.vehicleNumber}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = route.routeName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Driver: ${route.driverName} (${route.driverPhone})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Route Stops & Timings:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(4.dp))

          route.stops.forEach { stop ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(6.dp).background(RevenexBlue, CircleShape))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = stop, style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
    }
  }
}

@Composable
fun LibraryScreen(
  repository: ErpDataRepository
) {
  val books by repository.libraryBooks.collectAsState()
  val cloudMode = repository.dataMode.collectAsState().value == com.example.data.datasource.DataMode.SUPABASE_DEMO

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("library_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("School library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text(if (cloudMode) "Catalogue and availability. Borrower tracking and circulation are not enabled for this cloud demo." else "Local sample catalogue and circulation preview.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (books.isEmpty()) item { EmptyStateView("No books available", "The school catalogue has no accessible records yet.") }
    items(books, key = { it.id }) { book ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Book,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = book.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "By ${book.author}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${book.shelfLocation} • Available: ${book.availableCopies} / ${book.totalCopies}",
              style = MaterialTheme.typography.labelSmall,
              color = if (book.availableCopies > 0) SemanticColors.onSuccess else SemanticColors.onDanger,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          if (cloudMode) {
            Text("Catalogue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          } else if (book.availableCopies > 0) {
            OutlinedButton(
              onClick = { repository.issueBook(book.id) },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Text("Issue", fontSize = 12.sp)
            }
          } else {
            Button(
              onClick = { repository.returnBook(book.id) },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Text("Return", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun InventoryScreen(
  repository: ErpDataRepository
) {
  val inventory by repository.inventory.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("inventory_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = Spacing.xl24),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("School inventory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text("Recorded assets, quantities and locations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (inventory.isEmpty()) item { EmptyStateView("No assets recorded", "No inventory records are available for this school session.") }
    items(inventory, key = { it.id }) { item ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = item.itemName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.weight(1f)
            )
            StatusBadge(status = item.condition)
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Category: ${item.category} • Location: ${item.location}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Units: ${item.quantity}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Estimated Asset Value: ₹${item.estimatedValue.toInt()}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
