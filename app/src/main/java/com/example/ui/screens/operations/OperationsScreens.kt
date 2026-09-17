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
import com.example.data.model.LeaveStatus
import com.example.data.model.UserRole
import com.example.data.repository.ErpDataRepository
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

  data class HubModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val bgColor: Color,
    val route: String,
    val allowedRoles: Set<UserRole> = UserRole.values().toSet()
  )

  val allRoles = UserRole.values().toSet()
  val adminOnly = setOf(UserRole.PRINCIPAL)
  val staffOnly = setOf(UserRole.PRINCIPAL, UserRole.TEACHER)
  val noStudents = setOf(UserRole.PRINCIPAL, UserRole.TEACHER, UserRole.PARENT)

  val modules = listOf(
    HubModule("Student Directory", "Admissions & 360 Profiles", Icons.Default.Groups, ScholaSlateNavy, ScholaSlateContainer, Screen.Students.route, adminOnly),
    HubModule("Faculty & Staff", "Teacher Workloads & Departments", Icons.Default.School, ScholaSlateNavy, ScholaSlateContainer, Screen.Teachers.route, adminOnly),
    HubModule("Daily Attendance", "Mark and Review Registers", Icons.Default.FactCheck, ScholaSlateNavy, ScholaSlateContainer, Screen.Attendance.route, allRoles),
    HubModule("Fee Accounts", "Dues, Ledger & Receipts", Icons.Default.AccountBalanceWallet, ScholaSlateNavy, ScholaSlateContainer, Screen.Fees.route, setOf(UserRole.PRINCIPAL, UserRole.PARENT, UserRole.STUDENT)),
    HubModule("Leave Management", "Staff & Student Absences", Icons.Default.EventBusy, ScholaSlateNavy, ScholaSlateContainer, Screen.Leaves.route, noStudents),
    HubModule("Transport & Bus", "Routes, Stops & Drivers", Icons.Default.DirectionsBus, ScholaSlateNavy, ScholaSlateContainer, Screen.Transport.route, allRoles),
    HubModule("Digital Library", "Book Catalog, Issue & Return", Icons.Default.LocalLibrary, ScholaSlateNavy, ScholaSlateContainer, Screen.Library.route, allRoles),
    HubModule("School Assets", "Smart Boards, Labs & IT", Icons.Default.Inventory, ScholaSlateNavy, ScholaSlateContainer, Screen.Inventory.route, adminOnly),
    HubModule("Executive Reports", "KPI Charts & Export Analytics", Icons.Default.Analytics, ScholaSlateNavy, ScholaSlateContainer, Screen.Reports.route, adminOnly),
    HubModule("Timetable", "Class Schedule & Periods", Icons.Default.CalendarMonth, ScholaSlateNavy, ScholaSlateContainer, Screen.Timetable.route, allRoles),
    HubModule("Notices & Circulars", "School Announcements", Icons.Default.Campaign, ScholaSlateNavy, ScholaSlateContainer, Screen.Notices.route, allRoles),
    HubModule("Study Material", "Notes, PDFs & Resources", Icons.Default.MenuBook, ScholaSlateNavy, ScholaSlateContainer, Screen.StudyMaterial.route, allRoles),
    HubModule("AI Assistant", "Smart School Q&A & Search", Icons.Default.AutoAwesome, ScholaTerracotta, ScholaTerracottaContainer, Screen.AiAssistant.route, allRoles)
  ).filter { it.allowedRoles.contains(currentUser.role) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("operations_hub_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RevenexNavy)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Text(
            text = "Revenex ERP Unified Operations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Centralized School Management Modules & Workflows",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
    }

    item {
      Text(
        text = "All Operational Modules",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(modules.chunked(2)) { pair ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        pair.forEach { mod ->
          Card(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable { onNavigateTo(mod.route) }
              .testTag("module_card_${mod.title.lowercase().replace(" ", "_")}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .background(mod.bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = mod.icon,
                  contentDescription = null,
                  tint = mod.iconTint,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = mod.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = mod.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 2
              )
            }
          }
        }
        if (pair.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
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

  val pendingLeaves = leaveRequests.filter { it.status == LeaveStatus.PENDING }
  val historyLeaves = leaveRequests.filter { it.status != LeaveStatus.PENDING }

  Scaffold(
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onShowApplyLeave,
        icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
        text = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
        containerColor = RevenexBlue,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_apply_leave")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("leave_management_screen"),
      contentPadding = PaddingValues(bottom = 100.dp)
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

              if (leave.status == LeaveStatus.PENDING && currentUser.role == UserRole.PRINCIPAL) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  OutlinedButton(
                    onClick = { repository.rejectLeave(leave.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                  ) {
                    Text("Reject")
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    onClick = { repository.approveLeave(leave.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                  ) {
                    Text("Approve Leave")
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
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RevenexNavy)
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
                text = "GPS Fleet & Transport",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "3 Active Bus Routes • Live GPS Telemetry Enabled",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
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
                Text("LIVE GPS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
              }
            }
          }
        }
      }
    }

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
              color = RevenexPrimaryContainer
            ) {
              Text(
                text = route.routeNumber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RevenexBlue,
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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("library_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
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
              .background(ScholaSlateContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Book,
              contentDescription = null,
              tint = ScholaSlateNavy,
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
              color = if (book.availableCopies > 0) StatusSuccessText else StatusErrorText,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          if (book.availableCopies > 0) {
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
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
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
